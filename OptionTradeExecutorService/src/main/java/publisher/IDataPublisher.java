package publisher;

public interface IDataPublisher<T> {
    void publish(T data);
}
