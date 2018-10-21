package game;

class MoveFlags{

    public boolean moved;
    public boolean pressedGreenButton;
    public boolean pressedRedButton;
    public boolean pressedBrownButton;
    public boolean pressedBlueButton;
    public boolean sliding;
    public boolean creatureDied;
    public boolean enteredPortal;

    public MoveFlags(boolean moved, boolean pressedGreenButton, boolean pressedRedButton, boolean pressedBrownButton,
                     boolean pressedBlueButton, boolean sliding, boolean creatureDied, boolean enteredPortal){
        this.moved = moved;
        this.pressedGreenButton = pressedGreenButton;
        this.pressedRedButton = pressedRedButton;
        this.pressedBrownButton = pressedBrownButton;
        this.pressedBlueButton = pressedBlueButton;
        this.sliding = sliding;
        this.creatureDied = creatureDied;
        this.enteredPortal = enteredPortal;
    }

    public MoveFlags(boolean moved){
        this.moved = moved;
    }

    public static final MoveFlags FAIL = new MoveFlags(false);
    public static final MoveFlags SUCCESS = new MoveFlags(true);
    public static final MoveFlags DIED = new MoveFlags(true, false, false,
            false, false, false, true, false);
    public static final MoveFlags SLIDE = new MoveFlags(true, false, false,
            false, false, true, false, false);
    
    @Override
    public MoveFlags clone(){
        return new MoveFlags(moved,  pressedGreenButton,  pressedRedButton,  pressedBrownButton,
         pressedBlueButton,  sliding,  creatureDied,  enteredPortal);
    }

    public MoveFlags combineButtons(MoveFlags other){
        MoveFlags flags = clone();
        flags.pressedGreenButton ^= other.pressedGreenButton;
        flags.pressedBlueButton ^= other.pressedBlueButton;
        return flags;
    }

}
