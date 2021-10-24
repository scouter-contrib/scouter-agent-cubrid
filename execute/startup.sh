#/bin/sh
SHELL_PATH="$( cd "$( dirname "$0" )" && pwd -P )"

function show_usage ()
{
  echo "Usage: $0 [CMS USER_NAME] [CMS USER PASSWORD] [DBA USER NAME] [DBA USER PASSWORD]"
  echo " EXAMPLES :"
  echo "./startup.sh admin admin dba \"\""
  echo ""
}

if [ $# -eq 4 ]; then
  cms_user=$1
  cms_passwd=$2
  dba_user=$3
  dba_passwd=$4
else
  show_usage
  exit 0
fi

export agent_cms_user=$cms_user
export agent_cms_passwd=$cms_passwd
export agent_dba_user=$dba_user
export agent_dba_passwd=$dba_passwd

nohup java -Xmx128m -jar $SHELL_PATH/scouter-agent-cubrid.jar > nohup.out &
sleep 1
unset agent_cms_user
unset agent_cms_passwd
unset agent_dba_user
unset agent_dba_passwd
tail -100 nohup.out
