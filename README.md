# Multiplayer Death Swap
This is a spigot plugin for 1.15  that allows  you to play death swap.
It  utilizes  a  scoring system to allow many people to play without being
boring for those who die.

### Using the plugin


*Note that settings cannot be edited for an ongoing game. You must first run `/deathswap stop` to to edit settings.*

`/deathswap players [action] [playername]`, where `[action]` is either `add`
or `remove` to add or remove a player.
`/deathswap players addAll` to add all players.
`/deathswap players removeAll` to remove a player.
`/deathswap players list` to view the current players.

`/deathswap timer set [seconds]` to set the timer length.



### Development
This project uses maven.
You can build the jar file using  `$ mvn package`.