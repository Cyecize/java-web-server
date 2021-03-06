package com.cyecize.summer.areas.template.functions;

import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.validation.models.RedirectedBindingResult;
import org.jtwig.exceptions.JtwigException;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

public class JTwigFieldErrorsFunction extends SimpleJtwigFunction {

    private static final String INVALID_PARAMETER_MESSAGE = "FormErrors function accepts zero or one parameters of type String";

    private final DependencyContainer dependencyContainer;

    public JTwigFieldErrorsFunction(DependencyContainer dependencyContainer) {
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public String name() {
        return "formErrors";
    }

    /**
     * If there are no parameters, return all errors.
     * If there is one parameter of type String, return errors for a given field.
     * Else throw {@link JtwigException}
     */
    @Override
    public Object execute(FunctionRequest functionRequest) {
        final RedirectedBindingResult bindingResult = this.dependencyContainer.getService(RedirectedBindingResult.class);
        if (functionRequest.getNumberOfArguments() < 1) {
            return bindingResult.getErrors();
        }

        if (functionRequest.getNumberOfArguments() > 1 || functionRequest.getArguments().get(0) == null ||
                !(functionRequest.getArguments().get(0) instanceof String)) {
            throw new JtwigException(INVALID_PARAMETER_MESSAGE);
        }

        return bindingResult.getFieldErrors((String) functionRequest.getArguments().get(0));
    }
}
