package org.deegree.time.operator;

import static org.deegree.commons.tom.datetime.ISO8601Converter.parseDateTime;

import org.deegree.time.position.TimePosition;
import org.deegree.time.primitive.TimeGeometricPrimitive;
import org.deegree.time.primitive.TimeInstant;
import org.deegree.time.primitive.TimePeriod;
import org.deegree.time.primitive.TimePositionOrInstant;

public class AnyInteracts {

    public boolean anyInteracts( final TimeGeometricPrimitive t1, final TimeGeometricPrimitive t2 ) {
        if ( t1 == null || t2 == null ) {
            return false;
        }
        if ( t1 instanceof TimeInstant ) {
            return anyInteracts( ( (TimeInstant) t1 ).getPosition(), t2 );
        } else if ( t2 instanceof TimeInstant ) {
            return anyInteracts( ( (TimeInstant) t2 ).getPosition(), t1 );
        } else {
            return anyInteracts( (TimePeriod) t1, (TimePeriod) t2 );
        }
    }

    public boolean anyInteracts( final TimePosition t1, final TimeGeometricPrimitive t2 ) {
        if ( t2 instanceof TimeInstant ) {
            return anyInteracts( t1, ( (TimeInstant) t2 ).getPosition() );
        }
        return anyInteracts( t1, (TimePeriod) t2 );
    }

    public boolean anyInteracts( final TimePosition t1, final TimePosition t2 ) {
        if ( isIndeterminate( t1 ) || isIndeterminate( t2 ) ) {
            throw new RuntimeException();
        }
        return compare( t1, t2 ) == 0;
    }

    public boolean anyInteracts( final TimePosition t1, final TimePeriod t2 ) {
        if ( isIndeterminate( t1 ) ) {
            throw new RuntimeException();
        }
        if ( isBeginDeterminate( t2 ) ) {
            if ( compare( t1, t2.getBeginPosition() ) < 0 ) {
                return false;
            }
            if ( isEndDeterminate( t2 ) ) {
                return compare( t1, t2.getEndPosition() ) < 0;
            }
            return true;
        }
        if ( isEndDeterminate( t2 ) ) {
            return compare( t1, t2.getEndPosition() ) < 0;
        }
        return true;
    }

    public boolean anyInteracts( final TimePeriod t1, final TimePeriod t2 ) {
        return anyInteracts( t1.getBeginPosition(), t2 ) || anyInteracts( t2.getBeginPosition(), t1 );
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected int compare( final TimePosition t1, final TimePosition t2 ) {
        final Comparable value1 = getValue( t1 );
        final Object value2 = getValue( t2 );
        return value1.compareTo( value2 );
    }

    protected boolean isBeginDeterminate( final TimePeriod t ) {
        return t == null || getBegin( t ).getIndeterminatePosition() == null;
    }

    protected boolean isEndDeterminate( final TimePeriod t ) {
        return t == null || getEnd( t ).getIndeterminatePosition() == null;
    }

    protected TimePosition getBegin( final TimePeriod t ) {
        final TimePositionOrInstant begin = t.getBegin();
        if ( begin instanceof TimePosition ) {
            return (TimePosition) begin;
        }
        return ( (TimeInstant) begin ).getPosition();
    }

    protected TimePosition getEnd( final TimePeriod t ) {
        final TimePositionOrInstant end = t.getEnd();
        if ( end instanceof TimePosition ) {
            return (TimePosition) end;
        }
        return ( (TimeInstant) end ).getPosition();
    }

    protected boolean isIndeterminate( final TimePosition t ) {
        return t.getIndeterminatePosition() != null;
    }

    protected Comparable<org.deegree.commons.tom.datetime.TimeInstant> getValue( final TimePosition t ) {
        final String value = t.getValue();
        // TODO handle other types than dateTime
        return parseDateTime( value );
    }
}
