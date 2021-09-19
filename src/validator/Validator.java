package validator;

/**
 * Created by Akysh on 7/2/2017.
 */
public interface Validator<T> {

    public boolean validate(T t);
}
