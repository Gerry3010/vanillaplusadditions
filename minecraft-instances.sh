#!/bin/bash

# Minecraft Instance Manager
# Manages multiple Minecraft installations with different modsets

INSTANCES_DIR="$HOME/.minecraft-instances"
MINECRAFT_DIR="$HOME/.minecraft"
BACKUP_DIR="$MINECRAFT_DIR.backup"

create_instance() {
    local instance_name="$1"
    local instance_dir="$INSTANCES_DIR/$instance_name"
    
    if [ -z "$instance_name" ]; then
        echo "Usage: $0 create <instance_name>"
        exit 1
    fi
    
    echo "Creating instance: $instance_name"
    mkdir -p "$instance_dir"
    
    # Copy base minecraft structure
    if [ -d "$MINECRAFT_DIR" ]; then
        cp -r "$MINECRAFT_DIR"/* "$instance_dir/" 2>/dev/null || true
    fi
    
    # Create empty mods directory
    mkdir -p "$instance_dir/mods"
    
    echo "Instance '$instance_name' created at: $instance_dir"
    echo "Add mods to: $instance_dir/mods/"
}

switch_instance() {
    local instance_name="$1"
    local instance_dir="$INSTANCES_DIR/$instance_name"
    
    if [ -z "$instance_name" ]; then
        echo "Usage: $0 switch <instance_name>"
        list_instances
        exit 1
    fi
    
    if [ ! -d "$instance_dir" ]; then
        echo "Instance '$instance_name' does not exist!"
        list_instances
        exit 1
    fi
    
    # Backup current minecraft directory
    if [ -d "$MINECRAFT_DIR" ]; then
        echo "Backing up current .minecraft to .minecraft.backup"
        rm -rf "$BACKUP_DIR"
        mv "$MINECRAFT_DIR" "$BACKUP_DIR"
    fi
    
    # Create symlink to instance
    ln -sf "$instance_dir" "$MINECRAFT_DIR"
    echo "Switched to instance: $instance_name"
    echo "Launch Minecraft normally - it will use this instance"
}

list_instances() {
    echo "Available instances:"
    if [ -d "$INSTANCES_DIR" ]; then
        ls -1 "$INSTANCES_DIR" | sed 's/^/  - /'
    else
        echo "  No instances found"
    fi
    
    if [ -L "$MINECRAFT_DIR" ]; then
        local current=$(readlink "$MINECRAFT_DIR" | xargs basename)
        echo "Current instance: $current"
    else
        echo "Current instance: default"
    fi
}

restore_default() {
    if [ -L "$MINECRAFT_DIR" ]; then
        rm "$MINECRAFT_DIR"
        if [ -d "$BACKUP_DIR" ]; then
            mv "$BACKUP_DIR" "$MINECRAFT_DIR"
            echo "Restored default .minecraft directory"
        fi
    fi
}

case "$1" in
    "create")
        create_instance "$2"
        ;;
    "switch")
        switch_instance "$2"
        ;;
    "list")
        list_instances
        ;;
    "restore")
        restore_default
        ;;
    *)
        echo "Minecraft Instance Manager"
        echo "Usage: $0 {create|switch|list|restore} [instance_name]"
        echo ""
        echo "Commands:"
        echo "  create <name>  - Create a new instance"
        echo "  switch <name>  - Switch to an instance"
        echo "  list          - List all instances"
        echo "  restore       - Restore default .minecraft"
        echo ""
        list_instances
        ;;
esac