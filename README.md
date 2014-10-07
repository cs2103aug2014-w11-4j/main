RubberDuck
==========
Description
---------------------------
RubberDuck is a task manager catered for power users who love their keyboard.
It currently supports basic CRUD features and stores your data locally on your
system regardless of platform.

Available List of Commands
---------------------------
| Command Type  | Alias                      | Parameters                    |
| ------------- | -------------------------- | ------------------------------|
| Add           | `add` `insert` `ins` `new` | `description` `date(s)`       |
| View          | `view` `display`           | `date range` `all` `completed`|
| Search        | `find` `lookup` `search`   | `keyword`                     |
| Delete        | `delete` `remove`          | `ID`                          |
| Update        | `change` `update` `edit`   | `ID` `description` `date(s)`  |
| Undo          | `undo`                     |                               |
| Redo          | `redo`                     |                               |
| Mark          | `mark` `completed` `done`  | `ID`                          |
| Exit          | `exit` `quit`              |                               |
| Invalid       | all other alias            |                               |

CLI Output Format / Standards
-------------------------------
###Add
_______________________________
```
> add xxxx xxxx
> Your task has been added.
Task               Date
-----------------------------------------------------------
[XXXXXXXXXXXXXXXX] [XX XXX XXXX AM/PM to XX XXX XXXX AM/PM]
-----------------------------------------------------------
```
###View
_______________________________
```
> view xxxx
> You have [X] uncompleted tasks on .... / from ... to ... / remaining.
ID      Done   Task               Date
-----------------------------------------------------------
[XXXXX] [Y/N]  [XXXXXXXXXXXXXXXX] XX XXX XXXX AM/PM to
                                  XX XXX XXXX AM/PM
-----------------------------------------------------------
```

###Search
_______________________________
```
> search xxxx
> You have [X] uncompleted tasks that contains "xxxx".
ID      Done   Task               Date
-----------------------------------------------------------
[XXXXX] [Y/N]  [XXXXXXXXXXXXXXXX] XX XXX XXXX AM/PM to
                                  XX XXX XXXX AM/PM
-----------------------------------------------------------
```

###Delete
_______________________________
```
ID      Done   Task               Date
-----------------------------------------------------------
[XXXXX] [Y/N]  [XXXXXXXXXXXXXXXX] XX XXX XXXX AM/PM to
                                  XX XXX XXXX AM/PM
-----------------------------------------------------------
> delete XXXXX
> Task XXXXX have been deleted.
> <Message whether it is search or view>
ID      Done   Task               Date
-----------------------------------------------------------
[XXXXX] [Y/N]  [XXXXXXXXXXXXXXXX] XX XXX XXXX AM/PM to
                                  XX XXX XXXX AM/PM
-----------------------------------------------------------
```

###Update
_______________________________
```
ID      Done   Task               Date
-----------------------------------------------------------
[XXXXX] [Y/N]  [XXXXXXXXXXXXXXXX] XX XXX XXXX AM/PM to
                                  XX XXX XXXX AM/PM
-----------------------------------------------------------
> update XXXXX XXXXXXXXX
> Task XXXXX have been updated.
> <Message whether it is search or view>
ID      Done   Task               Date
-----------------------------------------------------------
[XXXXX] [Y/N]  [XXXXXXXXXXXXXXXX] XX XXX XXXX AM/PM to
                                  XX XXX XXXX AM/PM
-----------------------------------------------------------
```

###Undo
_____________________
```
> undo
> You have reverted on insertion of task. (TBA)
````

###Redo
_____________________
```
> redo
> You have redone on insertion of task. (TBA)
```

###Mark
_____________________
```
ID      Done   Task               Date
-----------------------------------------------------------
[XXXXX] [Y/N]  [XXXXXXXXXXXXXXXX] XX XXX XXXX AM/PM to
                                  XX XXX XXXX AM/PM
-----------------------------------------------------------
> mark XXXXX
> Task XXXXX is now mark as done/not done.
> <Message whether it is search or view>
ID      Done   Task               Date
-----------------------------------------------------------
[XXXXX] [Y/N]  [XXXXXXXXXXXXXXXX] XX XXX XXXX AM/PM to
                                  XX XXX XXXX AM/PM
-----------------------------------------------------------
```

###Exit
_____________________
```
> exit
```
