package com.cyecize.summer.areas.template.functions;

import org.jtwig.exceptions.JtwigException;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JTwigPathFunction extends SimpleJtwigFunction {

    private static final String INVALID_PARAM_ERROR = "Path function expects one parameter of type string.";

    private String routePrefix = "";

    public JTwigPathFunction(String appRootDir) {
        this.initRoutePrefix(appRootDir);
    }

    @Override
    public String name() {
        return "path";
    }

    @Override
    public Object execute(FunctionRequest functionRequest) {
        if (functionRequest.getArguments().size() != 1 || !(functionRequest.get(0) instanceof String)) {
            throw new JtwigException(INVALID_PARAM_ERROR);
        }
        return this.routePrefix + functionRequest.get(0);
    }

    /**
     * If the .jar file name is different that ROOT, add it to the route prefix.
     */
    private void initRoutePrefix(String appRootDir) {
        Pattern pattern = Pattern.compile("webapps(\\\\|\\/)(?!ROOT)(?<app>[a-zA-Z0-9-_ ]{1,})(\\\\|\\/)classes");
        Matcher matcher = pattern.matcher(appRootDir);
        if (matcher.find()) {
            this.routePrefix = "/" + matcher.group("app");
        }
    }
}
