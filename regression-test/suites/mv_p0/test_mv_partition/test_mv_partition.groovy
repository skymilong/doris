// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import org.codehaus.groovy.runtime.IOGroovyMethods

suite ("test_mv_partition") {
    sql """set enable_nereids_planner=true;"""
    sql """set enable_fallback_to_original_planner=false;"""
    sql """ DROP TABLE IF EXISTS chh; """

    sql """ 
        CREATE TABLE `chh` (
        `event_id` varchar(50) NULL COMMENT '',
        `time_stamp` datetime NULL COMMENT '',
        `device_id` varchar(150) NULL DEFAULT "" COMMENT ''
        ) ENGINE=OLAP
        DUPLICATE KEY(`event_id`)
        PARTITION BY RANGE(`time_stamp`)()
        DISTRIBUTED BY HASH(`device_id`) BUCKETS AUTO
        PROPERTIES (
        "replication_allocation" = "tag.location.default: 1",
        "is_being_synced" = "false",
        "dynamic_partition.enable" = "true",
        "dynamic_partition.time_unit" = "DAY",
        "dynamic_partition.time_zone" = "Asia/Shanghai",
        "dynamic_partition.start" = "-30",
        "dynamic_partition.end" = "1",
        "dynamic_partition.prefix" = "p",
        "dynamic_partition.replication_allocation" = "tag.location.default: 1",
        "dynamic_partition.buckets" = "20",
        "dynamic_partition.create_history_partition" = "true",
        "dynamic_partition.history_partition_num" = "-1",
        "dynamic_partition.hot_partition_num" = "0",
        "dynamic_partition.reserved_history_periods" = "NULL",
        "dynamic_partition.storage_policy" = "",
        "dynamic_partition.storage_medium" = "HDD",
        "storage_format" = "V2",
        "disable_auto_compaction" = "false",
        "enable_single_replica_compaction" = "false"
        ); 
        """

    sql """insert into chh(event_id,time_stamp,device_id) values('ad_sdk_request','2024-03-04 00:00:00','a');"""

    createMV("create materialized view m_view as select to_date(time_stamp),count(device_id) from chh group by to_date(time_stamp);")

    sql """insert into chh(event_id,time_stamp,device_id) values('ad_sdk_request','2024-03-04 00:00:00','a');"""
    
    qt_select_mv "select * from chh index m_view where `mv_to_date(time_stamp)` = '2024-03-04';"
}
