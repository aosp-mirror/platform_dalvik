#!/bin/bash

failed=0
for file in $(find $1 -type f -iname 'test*'); do
  case $file in
    *testFilters) continue; ;;
    *Expected) continue; ;;
    *Trace) continue; ;;
    *.html) continue; ;;
  esac

  echo "Running test for $file"

#  create_test_dmtrace $file tmp.trace
  dmtracedump -f testFilters -h "$file"Trace > tmp.html 2> /dev/null

  output=`diff tmp.html "$file"Expected 2>&1`
  if [ ${#output} -eq 0 ]
  then
    echo "  OK"
  else
    echo " Test failed: $output"
    failed=`expr $failed + 1`
  fi

done

rm tmp.trace
rm tmp.html

if [ $failed -gt 0 ]
then
  echo "$failed test(s) failed"
else
  echo "All tests passed successfully"
fi
