for i in $(seq -f "%02g" 1 25)
do
    printf "Parsing winzig_%s: " $i
    java winzigc -ast winzig_test_programs/winzig_${i} > tree.${i}
    DIFF=$(diff tree.${i} winzig_test_programs/winzig_${i}.tree)
    rm tree.${i}
    if [ "$DIFF" != "" ]
    then
        echo "false"
    else
        echo "true"
    fi
done
