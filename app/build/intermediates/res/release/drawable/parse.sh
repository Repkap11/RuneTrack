#!/bin/bash
echo -e "<string-array name=\"skill_names\">"
for fullpath in $(ls)
do
    filename="${fullpath##*/}"                      # Strip longest match of */ from start
    dir="${fullpath:0:${#fullpath} - ${#filename}}" # Substring from 0 thru pos of filename
    base="${filename%.[^.]*}"                       # Strip shortest match of . plus at least one non-dot char from end
    ext="${filename:${#base} + 1}"                  # Substring from len of base thru end
    if [[ -z "$base" && -n "$ext" ]]; then          # If we have an extension and no base, it's really the base
        base=".$ext"
        ext=""
    fi
    if [[ $base == *_* || $ext != png ]]
    then
    	base=""
    else 
    	echo -e "\t<item>$base</item>"
    fi

done
echo -e "</string-array>"
