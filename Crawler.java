import java.io.IOException;
import java.util.concurrent.*;

public class Crawler {
    static final String path = "https://yandex.ru/search/?lr=213&text=справочник";
    static final int maxDepth = 6;
    static final int threadCount = 20;

    static LinkStore storage = new LinkStore();

    static int workingCount = 0;
    static final Object monitor = new Object();
    static BlockingQueue<LinkDict> queue = new PriorityBlockingQueue<>();
    static LinkedBlockingQueue<Runnable> threadQueue = new LinkedBlockingQueue<>(threadCount);
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(threadCount, threadCount, 1, TimeUnit.MILLISECONDS, threadQueue);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) throws InterruptedException {

        /* Добавление в блокирующую очередь самой первой ссылки */
        queue.add(new LinkDict(path, 0));

        /* Создание потоков */
        for (int i=0; i < threadCount; i++) {
            executor.submit(new MyRunnable());
        }

        /* Ожидание завершения всех потоков */
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        /* Вывод всех найденых ссылок */
        for (LinkDict cell : storage.storage()) {
            System.out.println(cell.getDepth() + "\s" + cell.getLink());
        }

        System.out.println("Всего ссылок:\s" + storage.storage().size());

    }


    /* Потоковая ячейка */
    static class MyRunnable implements Runnable {

        @Override
        public void run(){
            System.out.println(storage.storage().size() + "\s" + queue.size() + "\s" + executor.getActiveCount() + "\s" + workingCount);

            try {
                /* Забор объекта ссылки из блокирующей очереди (если есть) иначе поток уходит в wait */
                LinkDict cell = queue.take();

                /* Прибавляем число рабочих потоков (т. е. находящиеся на стадии потенциального вычисления новых ссылок) */
                synchronized (monitor) {
                    workingCount++;
                }

                /* Проверка глубины */
                if (cell.getDepth() + 1 != maxDepth) {

                    /* Запрос страницы */
                    String text = MyRequest.httpsRequest(cell.getLink());
                    LinksWorker worker = new LinksWorker(text);
                    worker.removeRepeats();
                    worker.makeLinksValid(cell.getLink());
                    worker.filterCannotStartsWith("#");

                    /* forEach проход по всем полученным ссылкам */
                    for (String URL : worker.getList()) {
                        /* проверка на потворение хоста */
                        if (!storage.isHostAlreadyInList(URL)) {
                            LinkDict newCell = new LinkDict(URL, cell.getDepth() + 1);
                            queue.add(newCell);
                            /* Сохранение объекта ссылки в "долгую память" */
                            storage.store(newCell);
                        }
                    }
                }

            } catch (IOException ioe){
                // System.out.println("Ошибка сети:\s" + ioe.getMessage());
            } catch (InterruptedException ie){
                System.out.println("Принудительное завершение потока №" + Thread.currentThread().getId());
            } finally {
                /* Позавершению потенциального блока получения новых ссылок минусовние реально рабочих потоков */
                synchronized (monitor) {
                    workingCount--;
                    /* Проверка произошл ли вызов завершения всех потоков */
                    if (!executor.isShutdown()) {
                        /* Проыерка пуста ли очередь и количество рабочих потоков */
                        if (queue.isEmpty() && workingCount == 0) {
                        /*
                        Если нет потоков находящихся в работе (которые потенциально могут добавить новых ссылок)
                        то происходит завершение всех ждущих потоков
                        */
                            executor.shutdownNow();
                        } else {
                            /* Иначе происходит возобновление потока */
                            executor.submit(new MyRunnable());
                        }
                    }
                }
            }
        }
    }
}