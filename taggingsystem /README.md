# Tag-a-Bailiff Game


Start the server  `./r1_httpd.sh`

Start Java RMI daemon  `./r2_rmid.sh`

Start Jini look up service  `./r3_reggie.sh`

Set up th play field using the following command username and room-name are properties that describe a play field, minimum play field should consist of 3 Bailiffs.
`./bailiff.sh -user USERNAME_3 -room ROOM_3 -debug true`, replace 3 with any number or use your preferable naming convention. Use different terminals for each field&Bailiff

Start the agents(Dexter) of desired number. E.g two agents with Non-IT status and one agent with IT status as shown.
* `./dexter.sh -debug`
* `./dexter.sh -debug -it`

Observe the tagging.

# Improvements

Have tagging logic associated with agents and let the play field be a general container for any kind of operation.
Let the agent implement the tag method, have the agent retrieve a list of active agents, select an agent then send message to Bailiff, Bailiff then calls a remote method of agent to be tagged, if it goes successfully, then the Bailiff returns feedback to the IT agent if tagging was a success or not.
