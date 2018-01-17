import models.Token;
import org.junit.Test;
import validators.DNIValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit testing does not require Play application start up.
 *
 * https://www.playframework.com/documentation/latest/JavaTest
 */
public class UnitTest {

    @Test
    public void testRandomTokenCreation() {
        List<String> tokensList = new ArrayList<>();

        final int numTokens = 200;
        for (int i = 0; i < numTokens; i++) {
            tokensList.add(Token.generateToken());
        }

        Set<String> tokensSet = new HashSet<>(tokensList);

        assertThat(tokensSet.size()).isEqualTo(numTokens);
    }

    @Test
    public void testTokenLength() {
        String token = Token.generateToken();

        assertThat(token.length()).isEqualTo(20);
    }

    @Test
    public void testCorrectDNI() {
        DNIValidator validator = new DNIValidator();

        assertThat(validator.isValid("72654873N", null)).isTrue();
    }

    @Test
    public void testIncorrectDNI() {
        DNIValidator validator = new DNIValidator();

        assertThat(validator.isValid("72654873W", null)).isFalse();
    }
}
