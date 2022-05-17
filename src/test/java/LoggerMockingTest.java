import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.VoidAnswer2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Example which illustrates problems when mocking slf4j calls with Mockito
 *
 * @author Per Lundberg
 */
public class LoggerMockingTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggerMockingTest.class);

    @Test
    void logger_info_can_be_called_without_exceptions() {
        Logger mock = createMock(Logger.class);

        // The answerVoid() method here will attempt to cast String parameters to Object[], causing
        // a ClassCastException
        doAnswer(answerVoid((VoidAnswer2<String, Object[]>)
                logger::info
        )).when(mock)
                .info(any(), (Object[]) any());

        mock.info("Some message with {} {} {}", "three", "parameters", "");
    }

    // This works, but feels way more clumsy
    @Test
    void logger_info_can_be_called_without_exceptions_workaround() {
        Logger mock = createMock(Logger.class);

        doAnswer( invocation -> {
            String format = invocation.getArgument( 0 );
            Object[] allArguments = invocation.getArguments();
            Object[] arguments = Arrays.copyOfRange( allArguments, 1, allArguments.length );
            logger.info( format, arguments );
            return null;
        } ).when( mock )
                .info( anyString(), (Object[])any() );

        mock.info("Some message with {} {} {}", "three", "parameters", "");
    }

    public static <T> T createMock(Class<T> klass) {
        String context = klass.getSimpleName();

        return mock(klass, (Answer<Object>) invocation -> {
            String method = invocation.getMethod().toGenericString();
            throw new RuntimeException(context + " method not mocked: " + method);
        });
    }

}
