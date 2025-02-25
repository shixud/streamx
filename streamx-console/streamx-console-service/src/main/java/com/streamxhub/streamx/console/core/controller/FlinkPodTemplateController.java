/*
 * Copyright (c) 2019 The StreamX Project
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.streamxhub.streamx.console.core.controller;

import com.streamxhub.streamx.common.util.HostsUtils;
import com.streamxhub.streamx.console.base.domain.RestResponse;
import com.streamxhub.streamx.flink.kubernetes.PodTemplateParser;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Al-assad
 */
@Slf4j
@Validated
@RestController
@RequestMapping("flink/podtmpl")
public class FlinkPodTemplateController {

    @PostMapping("sysHosts")
    public RestResponse getHosts() {
        // hostname -> ipv4
        Map<String, String> hostMap = HostsUtils.getSystemHostsAsJava(true);
        List<String> friendlyHosts = hostMap.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList());
        return RestResponse.success(friendlyHosts);
    }

    @PostMapping("init")
    public RestResponse getInitContent() {
        return RestResponse.success(PodTemplateParser.getInitPodTemplateContent());
    }

    /**
     * @param hosts hostname:ipv4,hostname:ipv4,hostname:ipv4...
     */
    @PostMapping("compHostAlias")
    public RestResponse completeHostAlias(String hosts, String podTemplate) {
        Map<String, String> hostMap = covertHostsParamToMap(hosts);
        String completedPodTemplate = PodTemplateParser.completeHostAliasSpec(hostMap, podTemplate);
        return RestResponse.success(completedPodTemplate);
    }

    private Map<String, String> covertHostsParamToMap(String hosts) {
        if (StringUtils.isEmpty(hosts)) {
            return new HashMap<>(0);
        } else {
            return Arrays.stream(hosts.split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(e -> e.split(":"))
                .filter(arr -> arr.length == 2 && StringUtils.isNotBlank(arr[0]) && StringUtils.isNotBlank(arr[1]))
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
        }
    }

    @PostMapping("extractHostAlias")
    public RestResponse extractHostAlias(String podTemplate) {
        Map<String, String> hosts = PodTemplateParser.extractHostAliasMap(podTemplate);
        List<String> friendlyHosts = hosts.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.toList());
        return RestResponse.success(friendlyHosts);
    }

    /**
     * @param hosts hostname:ipv4,hostname:ipv4,hostname:ipv4...
     */
    @PostMapping("previewHostAlias")
    public RestResponse previewHostAlias(String hosts) {
        Map<String, String> hostMap = covertHostsParamToMap(hosts);
        String podTemplate = PodTemplateParser.previewHostAliasSpec(hostMap);
        return RestResponse.success(podTemplate);
    }

}
