/*
 * Copyright (c) 2007, Red Hat Middleware, LLC. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, v. 2.1. This program is distributed in the
 * hope that it will be useful, but WITHOUT A WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received a
 * copy of the GNU Lesser General Public License, v.2.1 along with this
 * distribution; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Red Hat Author(s): Steve Ebersole
 */
package org.fedorahosted.tennera.jgettext.util;

import java.util.Comparator;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Collection of utlities for dealing with {@link Locale locales}.
 *
 * @author Steve Ebersole
 */
public class LocaleUtils {

    public static final LocaleComparator LOCALE_COMPARATOR_INST =
            new LocaleComparator();

    public static class LocaleComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            return render((Locale) o1, '-').compareTo(render((Locale) o2, '-'));
        }
    }

    public static Locale parse(String locale) {
        return parse(locale, '-');
    }

    public static Locale parse(String locale, char sep) {
        StringTokenizer tokens = new StringTokenizer(locale, "" + sep);
        int tokencount = tokens.countTokens();
        switch (tokencount) {
        case 3:
            return new Locale(tokens.nextToken(), tokens.nextToken(),
                    tokens.nextToken());
        case 2:
            return new Locale(tokens.nextToken(), tokens.nextToken());
        case 1:
            return new Locale(tokens.nextToken());
        default:
            return new Locale("tbd");
        }
    }

    public static String render(Locale locale, char sep) {
        boolean l = locale.getLanguage().length() != 0;
        boolean c = locale.getCountry().length() != 0;
        boolean v = locale.getVariant().length() != 0;
        StringBuffer result = new StringBuffer(locale.getLanguage());
        if (c || (l && v)) {
            result.append(sep).append(locale.getCountry());
        }
        if (v && (l || c)) {
            result.append(sep).append(locale.getVariant());
        }
        return result.toString();
    }
}
