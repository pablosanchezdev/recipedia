package validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DNIValidator implements ConstraintValidator<DNI, String> {

    @Override
    public void initialize(DNI annotation) { }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && value.matches("[0-9]{8}[a-zA-Z]")
                && checkLetter(value);
    }

    // http://www.interior.gob.es/web/servicios-al-ciudadano/dni/calculo-del-digito-de-control-del-nif-nie
    private boolean checkLetter(String dni) {
        Character[] letters = {'T', 'R', 'W', 'A', 'G', 'M', 'Y', 'F', 'P', 'D', 'X',
                'B', 'N', 'J', 'Z', 'S', 'Q', 'V', 'H', 'L', 'C', 'K', 'E'};

        int rest = Integer.parseInt(dni.substring(0, 8)) % 23;

        return letters[rest].equals(dni.toUpperCase().charAt(8));
    }
}
