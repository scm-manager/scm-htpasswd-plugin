/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.javastack.scm.auth.htpasswd;

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
