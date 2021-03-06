/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.view.slider;

public enum AlertField {
  /**
   * Description of a service
   */
  description,
  /**
   * Host name where to which service belongs
   */
  host_name,
  /**
   * Last status
   */
  last_status,
  /**
   * Time when last status was checked
   */
  last_status_time,
  /**
   * Service
   */
  service_name,
  /**
   * Component name
   */
  component_name,
  /**
   * Same, as actual_status and last_status
   */
  status,
  /**
   * Time when status was checked
   */
  status_time,
  /**
   * Not yet used, for future purpose
   */
  output,
  /**
   * Same, as status and last_status
   */
  actual_status
}
