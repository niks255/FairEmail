git fetch fairemail
git merge $(git describe --tags $(git rev-list --tags --max-count=1))
$SHELL