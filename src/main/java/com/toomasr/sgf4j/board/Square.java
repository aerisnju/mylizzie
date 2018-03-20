package com.toomasr.sgf4j.board;

public class Square {
  private StoneState color;
  public int x;
  public int y;

  public Square(StoneState color, int x, int y) {
    this.color = color;
    this.x = x;
    this.y = y;
  }

  public Square(char colorChar, int x, int y) {
    if ('-' == colorChar)
      this.color = StoneState.EMPTY;
    else if ('o' == colorChar)
      this.color = StoneState.WHITE;
    else if ('x' == colorChar)
      this.color = StoneState.BLACK;

    this.x = x;
    this.y = y;
  }

  public Square(int x, int y) {
    this(StoneState.EMPTY, x, y);
  }

  public String toString() {
    if (this.color.equals(StoneState.WHITE))
      return "o";
    else if (this.color.equals(StoneState.BLACK))
      return "x";
    else
      return "-";
  }

  public boolean isEmpty() {
    return this.color.equals(StoneState.EMPTY);
  }

  public boolean isOfColor(StoneState theColor) {
    if (this.color.equals(theColor))
      return true;
    return false;
  }

  public StoneState getColor() {
    return this.color;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((color == null) ? 0 : color.hashCode());
    result = prime * result + x;
    result = prime * result + y;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Square other = (Square) obj;
    if (color != other.color)
      return false;
    if (x != other.x)
      return false;
    if (y != other.y)
      return false;
    return true;
  }

}
