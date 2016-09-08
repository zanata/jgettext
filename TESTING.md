# JGettext Test Suite

## Goals
 * Ensure parser supports all PO features
 * Ensure round-tripping of PO files work as expected (similar to GNU gettext)


## Approach
 * Create independent tests validating all features of PO files
 * Parse a large number of existing PO files and check for errors
 * Round-trip a large number of existing PO files and check for inconsitencies

# Plan
 * canonical roundtrip (RoundtripTest)
 * non-canonical but valid input to canonical output
 * in-memory model
 * character-set handling

## Pointers
### Translate Toolkit

    svn co https://translate.svn.sourceforge.net/svnroot/translate/src/trunk translate

Tests in `translate/storage/test_po.py`

### GNU Gettext
http://www.gnu.org/software/gettext/manual/gettext.html#PO-Files

    $ CVS_RSH=ssh cvs -z3 -d :pserver:anonymous@cvs.savannah.gnu.org:/sources/gettext co gettext 

Tests in `gettext-tools/tests`
