package com.cyecize.toyote.handlers;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.api.RequestHandler;
import com.cyecize.javache.api.RequestHandlerSharedData;

import com.cyecize.javache.api.SharedDataPropertyNames;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.exceptions.ResourceNotFoundException;
import com.cyecize.toyote.services.ResourceLocationService;
import com.cyecize.toyote.services.ResponsePopulationService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Request handler responsible for serving static resources such as images, html files, css, js and so on.
 */
@Service
public class ToyoteResourceHandler implements RequestHandler {

    private final ResourceLocationService resourceLocationService;

    private final ResponsePopulationService responsePopulationService;

    private final JavacheConfigService configService;

    @Autowired
    public ToyoteResourceHandler(
            ResourceLocationService resourceLocationService,
            ResponsePopulationService responsePopulationService,
            JavacheConfigService configService) {
        this.resourceLocationService = resourceLocationService;
        this.responsePopulationService = responsePopulationService;
        this.configService = configService;
    }

    @Override
    public void init() {
        this.responsePopulationService.init();
    }

    @Override
    public boolean handleRequest(InputStream inputStream, OutputStream outputStream, RequestHandlerSharedData sharedData)
            throws IOException {
        final HttpRequest request = sharedData.getObject(SharedDataPropertyNames.HTTP_REQUEST, HttpRequest.class);
        final HttpResponse response = sharedData.getObject(SharedDataPropertyNames.HTTP_RESPONSE, HttpResponse.class);

        try {
            final File resource = this.resourceLocationService.locateResource(request.getRequestURL());

            try (final FileInputStream fileInputStream = new FileInputStream(resource)) {
                this.responsePopulationService.handleResourceFoundResponse(request, response, resource, fileInputStream.available());

                outputStream.write(response.getBytes());
                this.transferStream(fileInputStream, outputStream);
            }

            return true;
        } catch (ResourceNotFoundException ignored) {
        }

        return false;
    }

    /**
     * The order of this request handler is configurable by the user.
     *
     * @return order of the request handler.
     */
    @Override
    public int order() {
        return this.configService.getConfigParam(JavacheConfigValue.TOYOTE_RESOURCE_HANDLER_ORDER, int.class);
    }

    private void transferStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        final byte[] buffer = new byte[2048];
        int read;

        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
    }
}
