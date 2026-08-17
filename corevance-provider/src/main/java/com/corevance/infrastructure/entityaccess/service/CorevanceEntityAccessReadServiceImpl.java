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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import com.corevance.infrastructure.entityaccess.data.CorevanceEntityRelationData;
import com.corevance.infrastructure.entityaccess.data.CorevanceEntityToEntityMappingData;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityAccessType;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityRelation;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityRelationRepositoryWrapper;
import com.corevance.infrastructure.entityaccess.domain.CorevanceEntityType;
import com.corevance.infrastructure.entityaccess.exception.CorevanceEntityMappingConfigurationException;
import com.corevance.infrastructure.security.service.PlatformSecurityContext;
import com.corevance.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class CorevanceEntityAccessReadServiceImpl implements CorevanceEntityAccessReadService {

    private final PlatformSecurityContext context;
    private final JdbcTemplate jdbcTemplate;
    private static final Logger LOG = LoggerFactory.getLogger(CorevanceEntityAccessReadServiceImpl.class);
    private final CorevanceEntityRelationRepositoryWrapper corevanceEntityRelationRepository;

    @Autowired
    public CorevanceEntityAccessReadServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate,
            final CorevanceEntityRelationRepositoryWrapper corevanceEntityRelationRepository) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
        this.corevanceEntityRelationRepository = corevanceEntityRelationRepository;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.corevance.infrastructure.entityaccess.service.
     * CorevanceEntityAccessReadService#getSQLQueryWithListOfIDsForEntityAccess (Long,
     * com.corevance.infrastructure.entityaccess.domain. CorevanceEntityType,
     * com.corevance.infrastructure.entityaccess.domain. CorevanceEntityAccessType,
     * com.corevance.infrastructure.entityaccess.domain. CorevanceEntityType, boolean)
     *
     * This method returns the list of entity IDs as a comma separated list Or null if there is no entity restrictions
     * or if there
     */
    @Override
    public String getSQLQueryInClause_WithListOfIDsForEntityAccess(CorevanceEntityType firstEntityType, final Long relId,
            final Long fromEntityId, boolean includeAllOffices) {
        Collection<CorevanceEntityToEntityMappingData> accesslist = retrieveEntityAccessFor(firstEntityType, relId, fromEntityId,
                includeAllOffices);
        String returnIdListStr = null;
        StringBuilder accessListCSVStrBuf = null;
        if ((accesslist != null) && (accesslist.size() > 0)) {
            for (CorevanceEntityToEntityMappingData accessData : accesslist) {
                if (accessData == null) {
                    throw new CorevanceEntityMappingConfigurationException();
                }

                if (accessListCSVStrBuf == null) {
                    accessListCSVStrBuf = new StringBuilder();
                } else {
                    accessListCSVStrBuf.append(",");
                }
                accessListCSVStrBuf.append(accessData.getToId());
                if (accessData.getToId() == 0) {
                    accessListCSVStrBuf = null;
                    break;
                }
            }

        } else {

            accessListCSVStrBuf = new StringBuilder();
            accessListCSVStrBuf.append("false"); // Append false so that no rows
                                                 // will be returned
        }
        if (accessListCSVStrBuf != null) {
            returnIdListStr = accessListCSVStrBuf.toString();
        }
        LOG.debug("List of IDs applicable: {}", returnIdListStr);
        return returnIdListStr;
    }

    @Override
    public Collection<CorevanceEntityToEntityMappingData> retrieveEntityAccessFor(CorevanceEntityType firstEntityType, final Long relId,
            final Long fromEntityId, boolean includeAllSubOffices) {
        final AppUser currentUser = this.context.authenticatedUser();

        final String hierarchy = currentUser.getOffice().getHierarchy();
        String hierarchySearchString = null;
        if (includeAllSubOffices) {
            hierarchySearchString = "." + "%";
        } else {
            hierarchySearchString = hierarchy + "%";
        }
        String sql = getSQLForRetriveEntityAccessFor();

        Collection<CorevanceEntityToEntityMappingData> entityAccessData = null;
        GetOneEntityMapper mapper = new GetOneEntityMapper();

        if (includeAllSubOffices && firstEntityType.getTableName().equals("m_office")) {
            sql += " where firstentity.hierarchy like ? order by firstEntity.hierarchy";
            entityAccessData = this.jdbcTemplate.query(sql, mapper, new Object[] { fromEntityId, fromEntityId, hierarchySearchString });
        } else {
            entityAccessData = this.jdbcTemplate.query(sql, mapper, new Object[] { relId, fromEntityId });
        }

        return entityAccessData;
    }

    @SuppressFBWarnings("SLF4J_SIGN_ONLY_FORMAT")
    private String getSQLForRetriveEntityAccessFor() {
        final String sql = """
                select  eem.rel_id as relId,eem.from_id as fromId,
                eem.to_id as toId, eem.start_date as startDate, eem.end_date as endDate
                from  m_entity_to_entity_mapping eem
                where eem.rel_id = ?
                and eem.from_id = ?\s""";
        LOG.debug("{}", sql);
        return sql;
    }

    @Override
    public String getSQLQueryInClauseIDList_ForLoanProductsForOffice(Long officeId, boolean includeAllOffices) {

        CorevanceEntityType firstEntityType = CorevanceEntityType.OFFICE;
        CorevanceEntityRelation corevanceEntityRelation = corevanceEntityRelationRepository
                .findOneByCodeName(CorevanceEntityAccessType.OFFICE_ACCESS_TO_LOAN_PRODUCTS.getStr());
        Long relId = corevanceEntityRelation.getId();
        return getSQLQueryInClause_WithListOfIDsForEntityAccess(firstEntityType, relId, officeId, includeAllOffices);
    }

    @Override
    public String getSQLQueryInClauseIDList_ForSavingsProductsForOffice(Long officeId, boolean includeAllOffices) {

        CorevanceEntityType firstEntityType = CorevanceEntityType.OFFICE;
        CorevanceEntityRelation corevanceEntityRelation = corevanceEntityRelationRepository
                .findOneByCodeName(CorevanceEntityAccessType.OFFICE_ACCESS_TO_SAVINGS_PRODUCTS.getStr());
        Long relId = corevanceEntityRelation.getId();

        return getSQLQueryInClause_WithListOfIDsForEntityAccess(firstEntityType, relId, officeId, includeAllOffices);
    }

    @Override
    public String getSQLQueryInClauseIDList_ForChargesForOffice(Long officeId, boolean includeAllOffices) {

        CorevanceEntityType firstEntityType = CorevanceEntityType.OFFICE;
        CorevanceEntityRelation corevanceEntityRelation = corevanceEntityRelationRepository
                .findOneByCodeName(CorevanceEntityAccessType.OFFICE_ACCESS_TO_CHARGES.getStr());
        Long relId = corevanceEntityRelation.getId();

        return getSQLQueryInClause_WithListOfIDsForEntityAccess(firstEntityType, relId, officeId, includeAllOffices);
    }

    @Override
    public Collection<CorevanceEntityRelationData> retrieveAllSupportedMappingTypes() {
        EntityRelationMapper entityMapper = new EntityRelationMapper();
        final String sql = entityMapper.schema();
        final Collection<CorevanceEntityRelationData> mapTypes = this.jdbcTemplate.query(sql, entityMapper, new Object[] {});
        return mapTypes;
    }

    private static final class EntityRelationMapper implements RowMapper<CorevanceEntityRelationData> {

        private static final String ENTITY_RELATION_SCHEMA = "select id as id,code_name as mapping_Types from m_entity_relation ";

        public String schema() {
            return ENTITY_RELATION_SCHEMA;
        }

        @Override
        public CorevanceEntityRelationData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long mappingTypesId = rs.getLong("id");
            final String mappingTypes = rs.getString("mapping_Types");
            return CorevanceEntityRelationData.getMappingTypes(mappingTypesId, mappingTypes);
        }
    }

    @Override
    public Collection<CorevanceEntityToEntityMappingData> retrieveEntityToEntityMappings(Long mapId, Long fromId, Long toId) {

        EntityToEntityMapper entityToEntityMapper = new EntityToEntityMapper();
        String sql = entityToEntityMapper.schema();
        final Collection<CorevanceEntityToEntityMappingData> mapTypes = this.jdbcTemplate.query(sql, entityToEntityMapper,
                new Object[] { mapId, fromId, fromId, toId, toId });
        return mapTypes;

    }

    @Override
    public Collection<CorevanceEntityToEntityMappingData> retrieveOneMapping(Long mapId) {
        GetOneEntityMapper entityMapper = new GetOneEntityMapper();
        String sql = entityMapper.schema();
        final Collection<CorevanceEntityToEntityMappingData> mapTypes = this.jdbcTemplate.query(sql, entityMapper, new Object[] { mapId });
        return mapTypes;
    }

    private static final class GetOneEntityMapper implements RowMapper<CorevanceEntityToEntityMappingData> {

        private static final String GET_ONE_ENTITY_SCHEMA = """
                select eem.rel_id as relId,
                eem.from_id as fromId,eem.to_Id as toId,eem.start_date as startDate,eem.end_date as endDate
                from m_entity_to_entity_mapping eem
                where eem.id= ?\s""";

        GetOneEntityMapper() {}

        public String schema() {
            return GET_ONE_ENTITY_SCHEMA;
        }

        @Override
        public CorevanceEntityToEntityMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {

            final Long relId = rs.getLong("relId");
            final Long fromId = rs.getLong("fromId");
            final Long toId = rs.getLong("toId");
            final Date startDate = rs.getDate("startDate");
            final Date endDate = rs.getDate("endDate");
            final LocalDate startLocalDate = startDate != null ? startDate.toLocalDate() : null;
            final LocalDate endLocalDate = endDate != null ? endDate.toLocalDate() : null;
            return CorevanceEntityToEntityMappingData.getRelatedEntities(relId, fromId, toId, startLocalDate, endLocalDate);
        }

    }

    private static final class EntityToEntityMapper implements RowMapper<CorevanceEntityToEntityMappingData> {

        private static final String ENTITY_TO_ENTITY_SCHEMA = """
                select eem.id as mapId,
                eem.rel_id as relId,
                eem.from_id as from_id,
                eem.to_id as to_id,
                eem.start_date as startDate,
                eem.end_date as endDate,
                case er.code_name
                when 'office_access_to_loan_products' then
                o.name
                when 'office_access_to_savings_products' then
                o.name
                when 'office_access_to_fees/charges' then
                o.name
                when 'role_access_to_loan_products' then
                r.name
                when 'role_access_to_savings_products' then
                r.name
                end as from_name,
                case er.code_name
                when 'office_access_to_loan_products' then
                lp.name
                when 'office_access_to_savings_products' then
                sp.name
                when 'office_access_to_fees/charges' then
                charge.name
                when 'role_access_to_loan_products' then
                lp.name
                when 'role_access_to_savings_products' then
                sp.name
                end as to_name,
                er.code_name
                from m_entity_to_entity_mapping eem
                join m_entity_relation er on eem.rel_id = er.id
                left join m_office o on er.from_entity_type = 1 and eem.from_id = o.id
                left join m_role r on er.from_entity_type = 5 and eem.from_id = r.id
                left join m_product_loan lp on er.to_entity_type = 2 and eem.to_id = lp.id
                left join m_savings_product sp on er.to_entity_type = 3 and eem.to_id = sp.id
                left join m_charge charge on er.to_entity_type = 4 and eem.to_id = charge.id
                where
                er.id = ? and
                ( ? = 0 or from_id = ? ) and
                ( ? = 0 or to_id = ? )\s""";

        EntityToEntityMapper() {}

        public String schema() {
            return ENTITY_TO_ENTITY_SCHEMA;
        }

        @Override
        public CorevanceEntityToEntityMappingData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum)
                throws SQLException {
            final Long mapId = rs.getLong("mapId");
            final Long relId = rs.getLong("relId");
            final Long fromId = rs.getLong("from_id");
            final Long toId = rs.getLong("to_id");
            final String fromEntity = rs.getString("from_name");
            final String toEntity = rs.getString("to_name");
            final Date startDate = rs.getDate("startDate");
            final Date endDate = rs.getDate("endDate");
            final LocalDate startLocalDate = startDate != null ? startDate.toLocalDate() : null;
            final LocalDate endLocalDate = endDate != null ? endDate.toLocalDate() : null;
            return CorevanceEntityToEntityMappingData.getRelatedEntities(mapId, relId, fromId, toId, startLocalDate, endLocalDate,
                    fromEntity, toEntity);
        }
    }

}
