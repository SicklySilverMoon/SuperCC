package game.button;

import game.Position;

public abstract class ConnectionButton extends Button {

    final Position targetPosition;
    
    public Position getTargetPosition() {
        return targetPosition;
    }
    
    public ConnectionButton(Position buttonPosition, Position targetPosition) {
        super(buttonPosition);
        this.targetPosition = targetPosition;
    }
    
}
