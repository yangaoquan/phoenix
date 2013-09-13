/*******************************************************************************
 * Copyright (c) 2013, Salesforce.com, Inc.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *     Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *     Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *     Neither the name of Salesforce.com nor the names of its contributors may 
 *     be used to endorse or promote products derived from this software without 
 *     specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package com.salesforce.hbase.index.covered;

import java.io.IOException;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.coprocessor.RegionCoprocessorEnvironment;


/**
 * Codec for creating index updates from the current state of a table
 */
public interface IndexCodec {

  /**
   * Do any code initialization necessary
   * @param env environment in which the codec is operating
   * @throws IOException if the codec cannot be initalized correctly
   */
  public void initialize(RegionCoprocessorEnvironment env) throws IOException;

  // JY: used for both batch case and covering delete case
  /**
   * Get the index cleanup entries. Currently, this must return just single row deletes (where just
   * the row-key is specified and no columns are returned) mapped to the table name. For instance,
   * to you have an index 'myIndex' with row :
   * 
   * <pre>
   * v1,v2,v3 | CF:CQ0  | rowkey
   *          | CF:CQ1  | rowkey
   * </pre>
   * 
   * To then cleanup this entry, you would just return 'v1,v2,v3', 'myIndex'.
   * @param state the current state of the table that needs to be cleaned up. Generally, you only
   *          care about the latest column values, for each column you are indexing for each index
   *          table.
   * @return the pairs of (deletes, index table name) that should be applied.
 * @throws IOException 
   */
  public Iterable<IndexUpdate> getIndexDeletes(TableState state) throws IOException;

  // table state has the pending update already applied, before calling
  // get the new index entries
  /**
   * Get the index updates for the primary table state, for each index table. The returned
   * {@link Put}s need to be fully specified (including timestamp) to minimize passes over the same
   * key-values multiple times.
   * <p>
   * You must specify the same timestamps on the Put as {@link TableState#getCurrentTimestamp()} so
   * the index entries match the primary table row. This could be managed at a higher level, but
   * would require iterating all the kvs in the Put again - very inefficient when compared to the
   * current interface where you must provide a timestamp anyways (so you might as well provide the
   * right one).
   * @param state the current state of the table that needs to an index update Generally, you only
   *          care about the latest column values, for each column you are indexing for each index
   *          table.
   * @return the pairs of (updates,index table name) that should be applied.
 * @throws IOException 
   */
  public Iterable<IndexUpdate> getIndexUpserts(TableState state) throws IOException;
}