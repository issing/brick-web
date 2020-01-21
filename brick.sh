#! /bin/bash

BASE_PATH=$(cd "$(dirname "$0")"; pwd)
BRICKS_COUNT=0
for _file in `ls`
do
    if [ -d $_file ] && [[ $_file == brick-* ]] 
    then
        BRICKS[$BRICKS_COUNT]=$_file
        #let BRICKS_COUNT=$BRICKS_COUNT+1
        ((BRICKS_COUNT++))
    fi
done

function Init()
{
    for _brick in ${BRICKS[@]}
    do
        cd $BASE_PATH/$_brick
        git init
        git remote add origin https://github.com/issing/${_brick}.git
    done
}

function Run()
{
    for _brick in ${BRICKS[@]}
    do
        cd $BASE_PATH/$_brick
        $*
    done
}

case "$1" in
    "clean")
        mvn clean ;;
    "install")
        mvn clean install ;;
    "deploy")
        mvn clean deploy -Poss,gpg ;;
    "init")
        Init ;;
    "commit")
        Run git "commit -m $2" ;;
    "checkout")
        Run git "checkout $2" ;;
    "push")
        Run git "push origin $2" ;;
    "pull")
        Run git "pull origin" ;;
    "tag")
        Run git "tag -a $2 -m $2" ;;
    "branch")
        Run git "checkout -b $2" ;;
    "fina")
        Run rm "-rf .git" ;;
    *)
        Run $* ;;
esac

cd $BASE_PATH
