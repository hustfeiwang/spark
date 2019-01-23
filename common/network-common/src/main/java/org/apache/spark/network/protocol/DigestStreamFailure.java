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

package org.apache.spark.network.protocol;

import com.google.common.base.Objects;
import io.netty.buffer.ByteBuf;

/**
 * Message indicating an error when transferring a stream.
 */
public final class DigestStreamFailure extends AbstractMessage implements ResponseMessage {
  public final String streamId;
  public final String error;

  public DigestStreamFailure(String streamId, String error) {
    this.streamId = streamId;
    this.error = error;
  }

  @Override
  public Type type() { return Type.DigestStreamFailure; }

  @Override
  public int encodedLength() {
    return Encoders.Strings.encodedLength(streamId) + Encoders.Strings.encodedLength(error);
  }

  @Override
  public void encode(ByteBuf buf) {
    Encoders.Strings.encode(buf, streamId);
    Encoders.Strings.encode(buf, error);
  }

  public static DigestStreamFailure decode(ByteBuf buf) {
    String streamId = Encoders.Strings.decode(buf);
    String error = Encoders.Strings.decode(buf);
    return new DigestStreamFailure(streamId, error);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(streamId, error);
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof DigestStreamFailure) {
      DigestStreamFailure o = (DigestStreamFailure) other;
      return streamId.equals(o.streamId) && error.equals(o.error);
    }
    return false;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("streamId", streamId)
      .add("error", error)
      .toString();
  }

}
