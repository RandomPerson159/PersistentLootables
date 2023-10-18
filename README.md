# Persistent Lootables
Functionality for time-based lootable containers

# Commands
/loot create mystery --> Uses the targeted container to create a mystery box
/loot create individual --> Uses the targeted container to create an individual box
/loot create everyone --> Uses the targeted container to create a shared everyone box
/loot check --> Checks the targeted block for lootable data
/loot set notify [true/false] --> Controls if the lootable should display information to the player
/loot set time [minutes] --> Sets the time before loot will be generated again (can be set to zero for testing)
/loot set amount [integer] --> Sets the amount of items to generate from the loot table
/loot set items --> Opens the loot table for editing

# Box Types
The only difference between them is their default values and generation behaviour

Mystery -->
      amount = 1
      time = 30 minutes
      notify = true
      loot-per-player
Individual -->
      amount = 27
      time = 60 minutes
      notify = true
      loot-per-player
Everyone -->
      amount = 27
      time = 20 minutes
      notify = false
      loot-to-first-player

# Limitations
Use single block containers (no double chests)
Works best with chests, barrels and shulker boxes

# Tips
Lootable containers can be duplicated with middle click
Once finished designing use /loot check
