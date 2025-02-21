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

package org.apache.shardingsphere.infra.config.datasource.jdbc.config;

import org.apache.shardingsphere.infra.config.datasource.jdbc.creator.JDBCDataSourceCreatorFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

/**
 * JDBC data source configuration.
 */
public abstract class JDBCDataSourceConfiguration {
    
    /**
     * Get type.
     *
     * @return type
     */
    public abstract String getType();
    
    /**
     * Get parameter.
     *
     * @return parameter
     */
    public abstract String getParameter();
    
    /**
     * Get data source configuration, related to {@link #getParameter()}.
     *
     * @return data source configuration
     */
    protected abstract Object getDataSourceConfiguration();
    
    /**
     * Append JDBC parameters.
     *
     * @param parameters JDBC parameters
     */
    public abstract void appendJDBCParameters(Map<String, String> parameters);
    
    /**
     * Get database type.
     *
     * @return database type
     */
    public abstract DatabaseType getDatabaseType();
    
    @Override
    public int hashCode() {
        return Objects.hash(getType(), getParameter());
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj || getClass() != obj.getClass()) {
            return false;
        }
        JDBCDataSourceConfiguration that = (JDBCDataSourceConfiguration) obj;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getParameter(), that.getParameter());
    }
    
    /**
     * Wrap.
     *
     * @return typed data source configuration wrap
     */
    public JDBCDataSourceConfigurationWrapper wrap() {
        JDBCDataSourceConfigurationWrapper result = new JDBCDataSourceConfigurationWrapper();
        result.setType(getType());
        result.setParameter(getParameter());
        return result;
    }
    
    /**
     * To data source.
     *
     * @return data source
     * @throws SQLException SQL exception
     */
    public DataSource toDataSource() throws SQLException {
        return JDBCDataSourceCreatorFactory.getInstance(getType()).createDataSource(getParameter(), getDataSourceConfiguration());
    }
}
