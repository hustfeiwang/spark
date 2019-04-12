/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.network.shuffle;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;

import org.apache.spark.network.util.DigestUtils;

/**
 * Keeps the index information for a particular map output
 * as an in-memory LongBuffer.
 */
public class ShuffleIndexInformation {
  /** offsets as long buffer */
  private final LongBuffer offsets;
  private final boolean hasDigest;
  /** digests as long buffer */
  private final LongBuffer digests;
  private int size;

  public ShuffleIndexInformation(File indexFile) throws IOException {
    ByteBuffer offsetsBuffer, digestsBuffer;
    size = (int)indexFile.length();
    int offsetsSize, digestsSize;
    if (size % 8 == 0) {
      hasDigest = false;
      offsetsSize = size;
      digestsSize = 0;
    } else {
      hasDigest = true;
      offsetsSize = ((size - 8 - 1) / (8 + DigestUtils.getDigestLength()) + 1) * 8;
      digestsSize = size - offsetsSize -1;
    }
    offsetsBuffer = ByteBuffer.allocate(offsetsSize);
    digestsBuffer = ByteBuffer.allocate(digestsSize);
    offsets = offsetsBuffer.asLongBuffer();
    digests = digestsBuffer.asLongBuffer();
    try (DataInputStream dis = new DataInputStream(Files.newInputStream(indexFile.toPath()))) {
      dis.readFully(offsetsBuffer.array());
      if (hasDigest) {
        dis.readByte();
      }
      dis.readFully(digestsBuffer.array());
    }
  }

  /**
   * If this indexFile has digest
   */
  public boolean isHasDigest() {
    return hasDigest;
  }

  /**
   * Size of the index file
   * @return size
   */
  public int getSize() {
    return size;
  }

  /**
   * Get index offset for a particular reducer.
   */
  public ShuffleIndexRecord getIndex(int reduceId) {
    long offset = offsets.get(reduceId);
    long nextOffset = offsets.get(reduceId + 1);
    /** Default digest is -1L.*/
    long digest = hasDigest ? digests.get(reduceId) : -1L;
    return new ShuffleIndexRecord(offset, nextOffset - offset, digest);
  }
}
