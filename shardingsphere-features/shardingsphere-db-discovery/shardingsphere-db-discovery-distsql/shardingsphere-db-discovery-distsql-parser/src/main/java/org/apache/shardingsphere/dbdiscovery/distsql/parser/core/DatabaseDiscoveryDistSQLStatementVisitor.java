/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.dbdiscovery.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.AbstractDatabaseDiscoverySegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryConstructionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryTypeSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryHeartbeatsStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.ShowDatabaseDiscoveryTypesStatement;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.AlterDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.CreateDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.CreateDatabaseDiscoveryTypeContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DatabaseDiscoveryRuleConstructionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DatabaseDiscoveryRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DatabaseDiscoveryTypeDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.DropDatabaseDiscoveryRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.ResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.ShowDatabaseDiscoveryHeartbeatsContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.ShowDatabaseDiscoveryRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.TypeDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.ShowDatabaseDiscoveryTypesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.TypePropertiesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DatabaseDiscoveryDistSQLStatementParser.TypePropertyContext;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * SQL statement visitor for database discovery dist SQL.
 */
public final class DatabaseDiscoveryDistSQLStatementVisitor extends DatabaseDiscoveryDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitCreateDatabaseDiscoveryRule(final CreateDatabaseDiscoveryRuleContext ctx) {
        return new CreateDatabaseDiscoveryRuleStatement(ctx.databaseDiscoveryRule().stream().map(each -> (AbstractDatabaseDiscoverySegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitAlterDatabaseDiscoveryRule(final AlterDatabaseDiscoveryRuleContext ctx) {
        return new AlterDatabaseDiscoveryRuleStatement(ctx.databaseDiscoveryRule().stream().map(each -> (AbstractDatabaseDiscoverySegment) visit(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitDatabaseDiscoveryRule(final DatabaseDiscoveryRuleContext ctx) {
        if (null != ctx.databaseDiscoveryRuleDefinition()) {
            return visit(ctx.databaseDiscoveryRuleDefinition());
        } else {
            return visit(ctx.databaseDiscoveryRuleConstruction());
        }
    }
    
    @Override
    public ASTNode visitDatabaseDiscoveryRuleConstruction(final DatabaseDiscoveryRuleConstructionContext ctx) {
        return new DatabaseDiscoveryConstructionSegment(getIdentifierValue(ctx.ruleName()), buildResources(ctx.resources()),
                getIdentifierValue(ctx.discoveryTypeName()), getIdentifierValue(ctx.discoveryHeartbeatName()));
    }
    
    @Override
    public ASTNode visitDatabaseDiscoveryRuleDefinition(final DatabaseDiscoveryRuleDefinitionContext ctx) {
        return new DatabaseDiscoveryDefinitionSegment(getIdentifierValue(ctx.ruleName()), buildResources(ctx.resources()),
                (AlgorithmSegment) visit(ctx.typeDefinition()), getProperties(ctx.discoveryHeartbeat().typeProperties()));
        
    }
    
    private List<String> buildResources(final ResourcesContext ctx) {
        return ctx.resourceName().stream().map(each -> new IdentifierValue(each.getText()).getValue()).collect(Collectors.toList());
    }
    
    @Override
    public ASTNode visitDropDatabaseDiscoveryRule(final DropDatabaseDiscoveryRuleContext ctx) {
        return new DropDatabaseDiscoveryRuleStatement(ctx.ruleName().stream().map(each -> getIdentifierValue(each)).collect(Collectors.toList()));
    }
    
    @Override
    public ASTNode visitShowDatabaseDiscoveryRules(final ShowDatabaseDiscoveryRulesContext ctx) {
        return new ShowDatabaseDiscoveryRulesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitCreateDatabaseDiscoveryType(final CreateDatabaseDiscoveryTypeContext ctx) {
        return new CreateDatabaseDiscoveryTypeStatement(buildAlgorithmEntry(ctx.databaseDiscoveryTypeDefinition()));
    }
    
    private Collection<DatabaseDiscoveryTypeSegment> buildAlgorithmEntry(final List<DatabaseDiscoveryTypeDefinitionContext> ctx) {
        return ctx.stream().map(each -> (DatabaseDiscoveryTypeSegment) visit(each)).collect(Collectors.toCollection(LinkedList::new));
    }
    
    @Override
    public ASTNode visitDatabaseDiscoveryTypeDefinition(final DatabaseDiscoveryTypeDefinitionContext ctx) {
        return new DatabaseDiscoveryTypeSegment(getIdentifierValue(ctx.discoveryTypeName()), (AlgorithmSegment) visit(ctx.typeDefinition()));
    }
    
    @Override
    public ASTNode visitShowDatabaseDiscoveryTypes(final ShowDatabaseDiscoveryTypesContext ctx) {
        return new ShowDatabaseDiscoveryTypesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShowDatabaseDiscoveryHeartbeats(final ShowDatabaseDiscoveryHeartbeatsContext ctx) {
        return new ShowDatabaseDiscoveryHeartbeatsStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        if (null == context) {
            return null;
        }
        return new IdentifierValue(context.getText()).getValue();
    }
    
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitTypeDefinition(final TypeDefinitionContext ctx) {
        return new AlgorithmSegment(getIdentifierValue(ctx.typeName()), null == ctx.typeProperties() ? new Properties() : getProperties(ctx.typeProperties()));
    }
    
    private Properties getProperties(final TypePropertiesContext ctx) {
        Properties result = new Properties();
        for (TypePropertyContext each : ctx.typeProperty()) {
            result.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
        }
        return result;
    }
}
