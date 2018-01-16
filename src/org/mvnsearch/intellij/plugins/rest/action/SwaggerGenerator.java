package org.mvnsearch.intellij.plugins.rest.action;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.StringUtils;
import org.mvnsearch.intellij.plugins.rest.HttpCall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * swagger generator
 *
 * @author linux_china
 */
public class SwaggerGenerator {
    private ObjectMapper objectMapper;
    private static SwaggerGenerator swaggerGenerator = new SwaggerGenerator();

    public static SwaggerGenerator getInstance() {
        return swaggerGenerator;
    }

    public SwaggerGenerator() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public List<HttpCall> parseSwagger(String location) {
        List<HttpCall> calls = new ArrayList<>();
        Swagger swagger = new SwaggerParser().read(location);
        String host = StringUtils.defaultIfEmpty(swagger.getHost(), "localhost");
        String schema = "http";
        if (swagger.getSchemes() != null && swagger.getSchemes().size() > 0) {
            schema = swagger.getSchemes().get(0).toValue();
        }
        String basePath = StringUtils.defaultIfEmpty(swagger.getBasePath(), "");
        Map<String, Path> paths = swagger.getPaths();
        for (Map.Entry<String, Path> pathEntry : paths.entrySet()) {
            Path path = pathEntry.getValue();
            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                HttpCall httpCall = new HttpCall();
                Operation operation = operationEntry.getValue();
                httpCall.setUrl(schema + "://" + host + basePath + pathEntry.getKey());
                httpCall.setAction(operationEntry.getKey().name());
                httpCall.setComment(StringUtils.defaultIfEmpty(operation.getDescription(), operation.getSummary()));
                List<Parameter> parameters = operation.getParameters();
                for (Parameter parameter : parameters) {
                    String name = parameter.getName();
                    if (parameter instanceof BodyParameter) {
                        Model model = ((BodyParameter) parameter).getSchema();
                        if (model instanceof RefModel) {
                            model = swagger.getDefinitions().get(((RefModel) model).getSimpleRef());
                        }
                        try {
                            Map<String, Object> body = new HashMap<>();
                            for (Map.Entry<String, Property> propertyEntry : model.getProperties().entrySet()) {
                                Property property = propertyEntry.getValue();
                                if (property instanceof RefProperty) {
                                    String simpleRef = ((RefProperty) property).getSimpleRef();
                                    swagger.getDefinitions().get(simpleRef);
                                }
                                body.put(propertyEntry.getKey(), "");
                            }
                            httpCall.setPayload(objectMapper.writeValueAsString(body));
                        } catch (JsonProcessingException ignore) {

                        }
                    }
                }
                calls.add(httpCall);
            }
        }
        return calls;
    }

}
