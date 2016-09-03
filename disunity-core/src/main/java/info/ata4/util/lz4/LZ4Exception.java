/* Partial import of https://github.com/jpountz/lz4-java, Apache 2.0 licensed. */

package info.ata4.util.lz4;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * LZ4 compression or decompression error.
 */
public class LZ4Exception extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public LZ4Exception(String msg, Throwable t) {
    super(msg, t);
  }

  public LZ4Exception(String msg) {
    super(msg);
  }

  public LZ4Exception() {
    super();
  }

}
