package com.demo.maniva.listener;

import java.util.List;

public interface MapboxListener {
    void onPermissionDenied();

    void locationEngineError();

    void onRouteError();

    void onExplanationNeeded(List<String> permissionsToExplain);
}
