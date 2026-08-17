/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.corevance.gradle

import com.corevance.gradle.service.JiraService
import org.gradle.api.Project

class CorevancePluginExtension {
    Project project
    Map<String, CorevancePluginStep> steps = [:]

    CorevancePluginExtension(Project project) {
        this.project = project
    }

    static class CorevancePluginConfig {
        String username
        String password
    }

    static class CorevancePluginConfigDoc {
        String url
        String directory
        String branch
    }

    static class CorevancePluginConfigJira {
        String url
        String username
        String password
        String project
    }

    static class CorevancePluginConfigConfluence {
        String url
        String username
        String password
        String project
    }

    static class CorevancePluginConfigGpg {
        String keyName
        String publicKeyring
        String secretKeyring
        String password
    }

    static class CorevancePluginConfigTemplate {
        String templateDir
    }

    static class CorevancePluginConfigSmtp {
        String host
        int port
        String username
        String password
        boolean tls
        boolean ssl
    }

    static class CorevancePluginConfigGit {
        String username
        String password
        String dir
        boolean dryRun
        List<CorevancePluginConfigGitSection> sections
    }

    static class CorevancePluginConfigGitSection {
        String section
        String subsection
        String name
        String value
    }

    static class CorevancePluginConfigSubversion {
        String username
        String password
        String revision
    }

    static class CorevancePluginStep {
        int order
        String description
        CorevancePluginEmailParams email
        CorevancePluginJiraParams jira
        CorevancePluginTemplateParams template
        CorevancePluginConfluenceParams confluence
        CorevancePluginGitParams git
        CorevancePluginSubversionParams subversion
        CorevancePluginGpgParams gpg
    }

    static class CorevancePluginEmailParams {
        String from
        String name
        String to
        String cc
        String bcc
        String mime
        String subject
        CorevancePluginTemplateParams subjectTemplate
        String message
        CorevancePluginTemplateParams messageTemplate
    }

    static class CorevancePluginJiraParams {
        String command
        String projectId
        String fields = "*all"
        String query
        List<JiraService.JiraIssue> result = new ArrayList<>()
        int pageOffset = 0
        int pageSize= 50
        int total = 1000
        List<String> includes = ["summary", "status", "assignee", "fixVersions"]
    }

    static class CorevancePluginConfluenceParams {
        String title
        String content
        Integer ancestor
    }

    static class CorevancePluginGitParams {
        String tag
        String message
        CorevancePluginTemplateParams messageTemplate
    }

    static class CorevancePluginSubversionParams {
        String url
        String command
        String revision = "HEAD"
        String directory
    }

    static class CorevancePluginTemplateParams {
        String template
        String templateFile
        String output
        String outputFile
    }

    static class CorevancePluginGpgParams {
        List<String> files
    }
}
