package com.cyecize.broccolina.services;

import com.cyecize.solet.HttpSolet;
import com.cyecize.solet.SoletConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ApplicationLoadingService {

    List<String> getApplicationNames();

    Map<String, HttpSolet> loadApplications(SoletConfig soletConfig) throws IOException;
}
