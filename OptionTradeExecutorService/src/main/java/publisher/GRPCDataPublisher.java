package publisher;


import awesome.code.base.service.exception.ServiceException;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class GRPCDataPublisher implements IDataPublisher<String> {

    private String projectId;
    private String topicId;
    private final Publisher publisher;

    public GRPCDataPublisher(String topicId, String projectId) throws ServiceException {
        this.projectId = projectId;
        this.topicId = topicId;
        TopicName topicName = TopicName.of(projectId, topicId);
        try {
            publisher = Publisher.newBuilder(topicName).build();
        } catch (IOException e) {
            System.out.println("Exception while connecting to topic for projectid " + projectId);
            throw new ServiceException(e);
        }
    }

    @Override
    public void publish(String data) {
        try {
            // Create a publisher instance with default settings bound to the topic

            ByteString dataToPublish = ByteString.copyFromUtf8(data);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(dataToPublish).build();
            // Once published, returns a server-assigned message id (unique within the topic)
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();
            System.out.println("Published message ID: " + messageId);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {

        }
    }
}
