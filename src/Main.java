import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static class Account {
        private long balance;
        private final Lock lock = new ReentrantLock();
        private final Condition sufficientFunds = lock.newCondition();
        public void deposit(long amount) {
            lock.lock();
            try {
                balance += amount;
                System.out.println("Пополнен счет на " + amount + ". Баланс: " + balance);
                sufficientFunds.signalAll();
            } finally {
                lock.unlock();
            }
        }
        public void withDraw(long amount) throws InterruptedException {
            lock.lock();
            try {
                while (balance < amount) {
                    sufficientFunds.await();
                }
                balance -= amount;
                System.out.println("Снято: " + amount + ". Остаток: " + balance);
            } finally {
                lock.unlock();
            }
        }
        public long getBalance() {
            lock.lock();
            try {
                return balance;
            } finally {
                lock.unlock();
            }
        }
    }
    public static void main(String[] args) {
        Account account = new Account();
        // Запускаем поток для пополнения счета
        Thread depositThread = new Thread(() -> {
            while (account.getBalance() < 5000) {
                long amount = (long) (Math.random() * 500 + 1);
                account.deposit(amount);
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        });
        depositThread.start();
        // Снимаем деньги
        try {
            account.withDraw(2000);
            // Ждем завершения выполнения кода на потоке пополнения
            depositThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Остаток на счете: " + account.getBalance());
    }
}