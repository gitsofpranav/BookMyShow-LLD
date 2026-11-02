public class Seat {
    private String seatId;
    private int row;
    private int seatNumber;
    private SeatType seatType;

    public Seat(String seatId, int row, int seatNumber, SeatType seatType){
        this.seatId = seatId;
        this.row = row;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public String getSeatId(){
        return seatId;
    }

    public int getRow(){
        return row;
    }

    public int getSeatNumber(){
        return seatNumber;
    }

    public SeatType getSeatType(){
        return seatType;
    }
}
