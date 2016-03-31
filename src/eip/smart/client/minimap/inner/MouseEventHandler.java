package eip.smart.client.minimap.inner;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public final class MouseEventHandler implements EventHandler<MouseEvent> {
	public interface onDragEvent {
		void handle(double movedX, double movedY);
	}

	private double		oldX		= Double.MIN_VALUE;
	private double		oldY		= Double.MIN_VALUE;

	private onDragEvent	onDragEvent	= null;

	@Override
	public void handle(MouseEvent event) {
		if (event.getEventType().equals(MouseEvent.MOUSE_RELEASED)) {
			this.oldX = Double.MIN_VALUE;
			this.oldY = Double.MIN_VALUE;
			return;
		}

		if (this.oldX != Double.MIN_VALUE && this.oldY != Double.MIN_VALUE) {
			double movedX = this.oldX - event.getX();
			double movedY = this.oldY - event.getY();
			if (this.onDragEvent != null)
				this.onDragEvent.handle(movedX, movedY);
		}
		this.oldX = event.getX();
		this.oldY = event.getY();
		event.consume();

	}

	public void setOnDragEvent(onDragEvent onDragEvent) {
		this.onDragEvent = onDragEvent;
	}
}