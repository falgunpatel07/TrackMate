package fiji.plugin.trackmate.util.cli.cellpose;

import java.awt.BorderLayout;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.commons.lang3.StringUtils;

import fiji.plugin.trackmate.util.cli.CliGuiBuilder;
import fiji.plugin.trackmate.util.cli.CommandBuilder;
import fiji.plugin.trackmate.util.cli.TrackMateSettingsBuilder;

public class ExampleCellposeCLI
{

	public static void main( final String[] args )
	{
		final int nbChannels = 3;
		final String spatialUnits = "µm";
		final CellposeCLI cli = new CellposeCLI( nbChannels, spatialUnits );

		/*
		 * Set values & create command line.
		 */

		cli.pretrainedModel().set( "cyto2" );
		cli.customModelPath().set( "/path/to/custom/model" );
		cli.mainChannel().set( 3 );
		cli.secondChannel().set( 2 );
		cli.cellDiameter().set( 40. );
		cli.useGPU().set();
		cli.inputFolder().set( System.getProperty( "user.home" ) + File.separator + "Desktop" );
		cli.selectPretrainedOrCustom().select( cli.pretrainedModel() );

		// Will generate an error if the required args are not set and have no
		// default.
		System.out.println( StringUtils.join( CommandBuilder.build( cli ), " " ) );

		/*
		 * Create GUI.
		 */

		final JPanel panel = CliGuiBuilder.build( cli );
		final JFrame frame = new JFrame( "Test CLI GUI" );
		frame.getContentPane().add( panel, BorderLayout.CENTER );
		final JButton btn = new JButton( "test" );
		final Map< String, Object > map = new HashMap<>();
		btn.addActionListener( e -> {
			System.out.println( "---" );
			System.out.println( CommandBuilder.build( cli ) );
			TrackMateSettingsBuilder.toTrackMateSettings( cli, map );
			System.out.println( map );
		} );
		frame.getContentPane().add( btn, BorderLayout.SOUTH );
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );

		/*
		 * Output.
		 */

//		System.out.println();
//		System.out.println( "All arguments:" );
//		cli.getArguments().forEach( System.out::println );
//
//		System.out.println();
//		System.out.println( "Selected arguments:" );
//		cli.getSelectedArguments().forEach( System.out::println );

	}
}
