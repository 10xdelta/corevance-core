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
package com.corevance.spm.util;

import java.util.ArrayList;
import java.util.List;
import com.corevance.infrastructure.core.service.DateUtils;
import com.corevance.portfolio.client.domain.Client;
import com.corevance.spm.data.ScorecardData;
import com.corevance.spm.data.ScorecardValue;
import com.corevance.spm.domain.Question;
import com.corevance.spm.domain.Response;
import com.corevance.spm.domain.Scorecard;
import com.corevance.spm.domain.Survey;
import com.corevance.spm.exception.SurveyResponseNotAvailableException;
import com.corevance.useradministration.domain.AppUser;

public final class ScorecardMapper {

    private ScorecardMapper() {

    }

    public static List<Scorecard> map(final ScorecardData scorecardData, final Survey survey, final AppUser appUser, final Client client) {
        final List<Scorecard> scorecards = new ArrayList<>();

        final List<ScorecardValue> scorecardValues = scorecardData.getScorecardValues();

        if (scorecardValues != null && !scorecardValues.isEmpty()) {
            for (ScorecardValue scorecardValue : scorecardValues) {
                final Scorecard scorecard = new Scorecard();
                scorecards.add(scorecard);
                scorecard.setSurvey(survey);
                ScorecardMapper.setQuestionAndResponse(scorecardValue, scorecard, survey);
                scorecard.setAppUser(appUser);
                scorecard.setClient(client);
                scorecard.setCreatedOn(DateUtils.getLocalDateTimeOfTenant());
                scorecard.setValue(scorecardValue.getValue());
            }
        } else {
            throw new SurveyResponseNotAvailableException();
        }
        return scorecards;
    }

    private static void setQuestionAndResponse(final ScorecardValue scorecardValue, final Scorecard scorecard, final Survey survey) {
        final List<Question> questions = survey.getQuestions();
        for (final Question question : questions) {
            if (question.getId().equals(scorecardValue.getQuestionId())) {
                scorecard.setQuestion(question);
                for (final Response response : question.getResponses()) {
                    if (response.getId().equals(scorecardValue.getResponseId())) {
                        scorecard.setResponse(response);
                        break;
                    }
                }
                break;
            }
        }
    }
}
