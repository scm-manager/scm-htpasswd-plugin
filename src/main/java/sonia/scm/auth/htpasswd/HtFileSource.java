/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.auth.htpasswd;

class HtFileSource {
  String[] splitKeyData(final String in) {
    final int offset = in.indexOf(":");
    if (offset < 0) {
      return null;
    }
    return new String[] {
        in.substring(0, offset), in.substring(offset + 1)
    };
  }

  // only safe chars: [0-9a-zA-Z]
  String cleanName(final String in) {
    final StringBuilder sb = new StringBuilder(in.length());
    for (int j = 0; j < in.length(); j++) {
      final char c = in.charAt(j);
      if ((c >= '0') && (c <= '9')) {
        sb.append(c);
      } else if ((c >= 'a') && (c <= 'z')) {
        sb.append(c);
      } else if ((c >= 'A') && (c <= 'Z')) {
        sb.append(c);
      }
    }
    return (in.length() == sb.length() ? in : sb.toString());
  }
}
