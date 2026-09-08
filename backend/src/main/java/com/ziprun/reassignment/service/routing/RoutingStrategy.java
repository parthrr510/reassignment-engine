package com.ziprun.reassignment.service.routing;

import com.ziprun.reassignment.domain.vo.RoutingRecommendation;
import com.ziprun.reassignment.domain.vo.RoutingRequest;

public interface RoutingStrategy {
    RoutingRecommendation route(RoutingRequest request);
}
