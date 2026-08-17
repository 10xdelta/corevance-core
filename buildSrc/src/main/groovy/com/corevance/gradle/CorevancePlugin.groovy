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

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.NotFileFilter
import org.apache.commons.io.filefilter.PrefixFileFilter
import com.corevance.gradle.service.*
import org.beryx.textio.TextIO
import org.beryx.textio.TextIoFactory
import org.beryx.textio.TextTerminal
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CorevancePlugin implements Plugin<Project> {
    private static final Logger log = LoggerFactory.getLogger(CorevancePlugin.class)

    private JiraService jiraService
    private ConfluenceService confluenceService
    private SubversionService subversionService
    private EmailService emailService
    private GpgService gpgService
    private TemplateService templateService
    private GitService gitService
    private Map<String, ?> context;

    static {
        System.setProperty("org.beryx.textio.TextTerminal", "org.beryx.textio.system.SystemTextTerminal")
    }

    void apply(Project project) {
        def extension = project.extensions.create("corevance", CorevancePluginExtension, project)

        project.corevance.extensions.create("config", CorevancePluginExtension.CorevancePluginConfig)
        project.corevance.config.extensions.create("doc", CorevancePluginExtension.CorevancePluginConfigDoc)
        project.corevance.config.extensions.create("template", CorevancePluginExtension.CorevancePluginConfigTemplate)
        project.corevance.config.extensions.create("smtp", CorevancePluginExtension.CorevancePluginConfigSmtp)
        project.corevance.config.extensions.create("subversion", CorevancePluginExtension.CorevancePluginConfigSubversion)
        project.corevance.config.extensions.create("jira", CorevancePluginExtension.CorevancePluginConfigJira)
        project.corevance.config.extensions.create("confluence", CorevancePluginExtension.CorevancePluginConfigConfluence)
        project.corevance.config.extensions.create("gpg", CorevancePluginExtension.CorevancePluginConfigGpg)
        project.corevance.config.extensions.create("git", CorevancePluginExtension.CorevancePluginConfigGit)

        project.afterEvaluate {
            this.templateService = new TemplateService(extension.config.template)
            this.jiraService = new JiraService(extension.config.jira)
            this.confluenceService = new ConfluenceService(extension.config.confluence)
            this.subversionService = new SubversionService(extension.config.subversion)
            this.emailService = new EmailService(extension.config.smtp)
            this.gitService = new GitService(extension.config.git, extension.config.gpg)
            this.context = context(project)
        }

        project.tasks.register("corevanceDocPublish") {
            dependsOn(":corevance-doc:doc")

            doLast {
                log.warn("Corevance Publish Doc...")

                def url = extension.config.doc.url?:"git@github.com:apache/corevance-site.git";
                def branch = extension.config.doc.branch?:"asf-site"
                def directory = extension.config.doc.directory?:System.getProperty("java.io.tmpdir")+"/corevance-site"

                def d = new File(directory)

                if(d.exists()) {
                    gitService.pull(directory)
                } else {
                    gitService.clone(url, branch, directory)

                    gitService.config(directory, extension.config.git.sections)
                }

                def source = new File("corevance-doc/build/docs/html/en");
                def target = new File(directory + "/docs/current")

                if(!target.name) {
                    target.mkdirs()
                }

                FileUtils.copyDirectory(source, target, new NotFileFilter(new PrefixFileFilter(".asciidoctor")), true)

                gitService.commit(directory)
            }
        }

        project.tasks.register("corevanceReleaseTest") {
            doFirst {
                // emojis:
                // https://gist.github.com/parmentf/035de27d6ed1dce0b36a
                // https://emojipedia.org/
                // https://unicode.org/emoji/charts/full-emoji-list.html

                CorevancePluginExtension.CorevancePluginStep step = step(extension, "test")

                // TODO:
                // - jira:
                //   - move tickets to new version

                // jira query
                CorevancePluginExtension.CorevancePluginJiraParams result = jiraService.search(step.jira)
                log.warn(">>>>>>>>>>>>>>>> ISSUE: ${result.result[0]?.id} - ${result.result[0]?.key} - ${result.result[0]?.fields?['summary']} - ${result.result[0]?.fields?['fixVersions']}")

                /*

                // confluence create
                def content = new ConfluenceService.ConfluenceContent(title: "1.7.0 - Apache Corevance",
                        space: new ConfluenceService.ConfluenceSpace(key: "COREVANCE"),
                        ancestors: [new ConfluenceService.ConfluenceParent(id: "75974324")],
                        body: new ConfluenceService.ConfluenceBody(storage: new ConfluenceService.ConfluenceStorage(value: "<h1>Release Artifacts</h1>")))
                confluenceService.createContent(content)

                // confluence get
                ConfluenceService.ConfluenceContent content = confluenceService.getContent("211881930")
                log.warn(">>>>>>>>>>>>>>>>>>>>> CONFLUENCE: ${content.id} - ${content.title} - ${content.body?.storage?.value}")

                // confluence update
                content.body?.storage?.value = "<h1>Release Artifacts</h1><p><ac:structured-macro ac:macro-id=\"ac1cb3cb-84d4-45aa-820b-05cc42d39dd9\" ac:name=\"jira\" ac:schema-version=\"1\"><ac:parameter ac:name=\"server\">ASF JIRA</ac:parameter><ac:parameter ac:name=\"columnIds\">issuekey,summary,issuetype,created,updated,duedate,assignee,reporter,customfield_12311032,customfield_12311037,customfield_12311022,customfield_12311027,priority,status,resolution</ac:parameter><ac:parameter ac:name=\"columns\">key,summary,type,created,updated,due,assignee,reporter,Priority,Priority,Priority,Priority,priority,status,resolution</ac:parameter><ac:parameter ac:name=\"maximumIssues\">50</ac:parameter><ac:parameter ac:name=\"jqlQuery\">project = COREVANCE AND fixVersion = 1.7.0 AND status not in (Open)</ac:parameter><ac:parameter ac:name=\"serverId\">5aa69414-a9e9-3523-82ec-879b028fb15b</ac:parameter></ac:structured-macro></p><p><br/></p>"
                content.space = new ConfluenceService.ConfluenceSpace(key: "COREVANCE")
                content.status = null
                content.version = new ConfluenceService.ConfluenceVersion(number: 3)

                // confluence update
                content = confluenceService.updateContent(content.id, content)
                log.warn(">>>>>>>>>>>>>>>>>>>>> CONFLUENCE: ${content.id} - ${content.title} - ${content.body?.storage?.value}")

                // confluence delete
                ConfluenceService.ConfluenceResponse response = confluenceService.deleteContent("211881930")
                log.warn(">>>>>>>>>>>>>>>>>>>>> CONFLUENCE: ${response?.statusCode} - ${response?.message}")

                // jira versions
                def versions = jiraService.getProjectVersions("12319420")
                versions.findAll {
                    log.warn(">>>> VERSION: ${it.id} - ${it.name} - ${it.description}")
                }

                def version = jiraService.getVersion("12351431")
                log.warn(">>>> !!! VERSION: ${version.id} - ${version.name} - ${version.description} - ${version.archived}")

                version.archived = !version.archived

                jiraService.updateVersion(version)
                version = jiraService.getVersion("12351431")
                log.warn(">>>> === VERSION: ${version.id} - ${version.name} - ${version.description} - ${version.archived}")

                // jira projects
                def projects = jiraService.getProjects()

                projects.findAll {
                    log.warn(">>>>>>>>>>>> PROJECT: ${it.id} - ${it.key} - ${it.name}")
                }

                // jira info
                Map<String, Object> info = jiraService.serverInfo()
                log.warn(">>>>>>>>>>>>>>>> INFO: ${info}")

                // jira query
                CorevancePluginExtension.CorevancePluginJiraParams result = jiraService.search(step.jira)
                log.warn(">>>>>>>>>>>>>>>> INFO: ${result.result.size()}")

                // format with template
                def tmp = templateService.render(step.template, [issues: result.result])

                log.warn(tmp.output)
                */
            }
        }

        // TODO: step for creating release Jira ticket missing!

        // step 1
        project.tasks.register("corevanceReleaseStep1") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step1")

                String version = project.properties?['corevance.release.version']
                String issue = project.properties?['corevance.release.issue']
                String date = project.properties?['corevance.releaseBranch.date']

                if(!version || !issue || !date) {
                    TextIO textIO = TextIoFactory.getTextIO()

                    if(!version) {
                        version = textIO.newStringInputReader()
                                .withPattern("\\d+.\\d+.\\d+")
                                .read("Release Version");
                    }
                    if(!issue) {
                        issue = textIO.newStringInputReader()
                                .withMaxLength(4)
                                .withPattern("\\d+")
                                .read("Jira Issue");
                    }
                    if(!date) {
                        date = textIO.newStringInputReader()
                                .withMaxLength(10)
                                .withPattern("\\d\\d\\d\\d-\\d\\d-\\d\\d")
                                .read("Date");
                    }

                    TextTerminal terminal = textIO.getTextTerminal()
                    terminal.printf("\nResult: %s - COREVANCE-%s - %s", version, issue, date)
                }

                // TODO: input validation, see COREVANCE-1610

                this.context?.project?['corevance.release.version'] = version
                this.context?.project?['corevance.release.issue'] = issue
                this.context?.project?['corevance.releaseBranch.date'] = date

                if(step.email) {
                    emailService.send( processEmailParams(step.email, this.context) )
                }
            }
        }

        // step 2
        project.tasks.register("corevanceReleaseStep2") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step2")

                // TODO: implement this, see COREVANCE-1611

                printInstructions(project, "step2")
            }
        }

        // step 3
        project.tasks.register("corevanceReleaseStep3") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step3")

                String version = project.properties?['corevance.release.version']
                String date = project.properties?['corevance.release.date']

                if(!version || !date) {
                    TextIO textIO = TextIoFactory.getTextIO()

                    if(!version) {
                        version = textIO.newStringInputReader()
                                .withPattern("\\d+.\\d+.\\d+")
                                .read("Release Version");
                    }
                    if(!date) {
                        date = textIO.newStringInputReader()
                                .withMaxLength(10)
                                .withPattern("\\d\\d\\d\\d-\\d\\d-\\d\\d")
                                .read("Date");
                    }

                    TextTerminal terminal = textIO.getTextTerminal()
                    terminal.printf("\nResult: %s - %s", version, date)
                }

                // TODO: input validation, see COREVANCE-1610

                // TODO: create release branch, see COREVANCE-1611

                this.context?.project?['corevance.release.version'] = version
                this.context?.project?['corevance.release.date'] = date

                if(step.email) {
                    emailService.send( processEmailParams(step.email, this.context) )
                }
            }
        }

        // step 4
        project.tasks.register("corevanceReleaseStep4") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step4")
                // TODO: implement this, see COREVANCE-1611

                printInstructions(project, "step4")
            }
        }

        // step 5
        project.tasks.register("corevanceReleaseStep5") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step5")

                String version = project.properties?['corevance.release.version']

                if(!version) {
                    TextIO textIO = TextIoFactory.getTextIO()

                    if (!version) {
                        version = textIO.newStringInputReader()
                                .withPattern("\\d+.\\d+.\\d+")
                                .read("Release Version");
                    }
                }

                this.context?.project?['corevance.release.version'] = version

                def params = processGitParams(step.git, this.context)

                params.tag = params.tag?:version

                gitService.createTag(params.tag, params.message)
            }
        }

        // step 6
        project.tasks.register("corevanceReleaseStep6") {
            dependsOn(":corevance-war:distTar")
        }

        // step 7
        project.tasks.register("corevanceReleaseStep7") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step7")
                gpgService = new GpgService(extension.config.gpg)
                gpgService.sign(step.gpg)

                step.gpg.files.findAll {
                    gpgService.sha512(step.gpg)
                }
            }
        }

        // step 8
        project.tasks.register("corevanceReleaseStep8") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step8")

                String version = project.properties?['corevance.release.version']

                if(!version) {
                    TextIO textIO = TextIoFactory.getTextIO()

                    version = textIO.newStringInputReader()
                            .withPattern("\\d+.\\d+.\\d+")
                            .read("Release Version");
                }

                // TODO: input validation, see COREVANCE-1610

                subversionService.checkout(step.subversion)

                def directory = step.subversion.directory?:System.getProperty("java.io.tmpdir") + "/corevance-dist-dev"

                def source = new File("corevance-war/build/distributions")
                def target = new File("${directory}/${version}")

                FileUtils.copyDirectory(source, target, true)

                subversionService.commit(step.subversion)
            }
        }

        // step 9
        project.tasks.register("corevanceReleaseStep9") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step9")
                // TODO: implement this, see COREVANCE-1611

                printInstructions(project, "step9")
            }
        }

        // step 10
        project.tasks.register("corevanceReleaseStep10") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step10")

                printInstructions(project, "step10")
            }
        }

        // step 11
        project.tasks.register("corevanceReleaseStep11") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step11")

                printInstructions(project, "step11")
            }
        }

        // step 12
        project.tasks.register("corevanceReleaseStep12") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step12")
                // TODO: input validation, see COREVANCE-1610

                // TODO: implement this, see COREVANCE-1817
                printInstructions(project, "step12")
            }
        }

        // step 13
        project.tasks.register("corevanceReleaseStep13") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step13")
                // TODO: implement this, see COREVANCE-1611

                printInstructions(project, "step13")
            }
        }

        // step 14
        project.tasks.register("corevanceReleaseStep14") {
            doFirst {
                CorevancePluginExtension.CorevancePluginStep step = step(extension, "step14")
                // TODO: implement this, see COREVANCE-1611

                printInstructions(project, "step14")
            }
        }

        // step 15
        project.tasks.register("corevanceReleaseStep15") {
            doFirst {
                log.warn("Release step 15: send email to announcement mailing list")

                printInstructions(project, "step15")
            }
        }
    }

    private CorevancePluginExtension.CorevancePluginStep step(CorevancePluginExtension extension, String id) {
        CorevancePluginExtension.CorevancePluginStep step = extension.steps[id]

        if(step) {
            log.warn("Corevance release step ${step.order}: ${step.description}.")
        } else {
            throw new RuntimeException("Could not find any parameters for step with ID '${id}'")
        }

        return step
    }

    private CorevancePluginExtension.CorevancePluginEmailParams processEmailParams(CorevancePluginExtension.CorevancePluginEmailParams params, Object data) {
        if(params.subjectTemplate) {
            def result = templateService.render(params.subjectTemplate, data)

            params.subject = result.output
        }
        if(params.messageTemplate) {
            def result = templateService.render(params.messageTemplate, data)

            params.message = result.output
        }

        return params
    }

    private CorevancePluginExtension.CorevancePluginGitParams processGitParams(CorevancePluginExtension.CorevancePluginGitParams params, Object data) {
        if(params.messageTemplate) {
            def result = templateService.render(params.messageTemplate, data)

            params.message = result.output
        }

        return params
    }

    private Map<String, ?> context(Project project) {
        return Map.of("project", project.getProperties().findAll { it.key != "password"})
    }

    private void printInstructions(Project project, String step) {
        String version = project.properties?['corevance.release.version']?:"0.0.0"

        this.context?.project?['corevance.release.version'] = version

        CorevancePluginExtension.CorevancePluginTemplateParams result = templateService.render(new CorevancePluginExtension.CorevancePluginTemplateParams(templateFile: new File("buildSrc/src/main/resources/instructions/${step}.txt.ftl")), this.context)

        log.warn(result.output)
    }
}
