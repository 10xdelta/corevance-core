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
package com.corevance.infrastructure.entityaccess.service;

import java.util.Collection;
import com.corevance.infrastructure.entityaccess.data.CorevanceEntityRelationData;
import com.corevance.infrastructure.entityaccess.data.CorevanceEntityToEntityMappingData;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityType;

public interface CorevanceEntityAccessReadService {

    Collection<CorevanceEntityToEntityMappingData> retrieveEntityAccessFor(CorevanceEntityType firstEntityType, Long relId, Long fromEntityId,
            boolean includeAllSubOffices);

    String getSQLQueryInClause_WithListOfIDsForEntityAccess(CorevanceEntityType firstEntityType, Long relId, Long fromEntityId,
            boolean includeAllOffices);

    String getSQLQueryInClauseIDList_ForLoanProductsForOffice(Long loanProductId, boolean includeAllOffices);

    String getSQLQueryInClauseIDList_ForSavingsProductsForOffice(Long savingsProductId, boolean includeAllOffices);

    String getSQLQueryInClauseIDList_ForChargesForOffice(Long officeId, boolean includeAllOffices);

    Collection<CorevanceEntityRelationData> retrieveAllSupportedMappingTypes();

    Collection<CorevanceEntityToEntityMappingData> retrieveOneMapping(Long mapId);

    Collection<CorevanceEntityToEntityMappingData> retrieveEntityToEntityMappings(Long mapId, Long fromoId, Long toId);

}
