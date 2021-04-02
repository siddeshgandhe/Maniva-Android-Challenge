package com.demo.maniva.listener;

import java.util.List;

public interface MapboxListener {
    void onPermissionDenied();

    void onLocationEngineError();

    void onRouteError();

    void onExplanationNeeded(List<String> permissionsToExplain);
}
