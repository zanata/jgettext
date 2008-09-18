/*
 * Copyright (c) 2007, Red Hat Middleware, LLC. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, v. 2.1. This program is distributed in the
 * hope that it will be useful, but WITHOUT A WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received a
 * copy of the GNU Lesser General Public License, v.2.1 along with this
 * distribution; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Red Hat Author(s): Steve Ebersole
 */
package org.jboss.jdocbook.render.format;

import java.io.File;

import org.codehaus.plexus.util.FileUtils;
import org.jboss.jdocbook.util.DocBookProfilingStrategy;

/**
 * Merging of DocBook standard information and user specifics.  The combination represents the information that will
 * control the format processing.
 *
 * @author Steve Ebersole
 */
public class FormatPlan {
	private final String name;
	private final String stylesheetResource;
	private final String correspondingDocBookStylesheetResource;
	private final boolean imagePathSettingRequired;
	private final boolean imageCopyingRequired;
	private final boolean doingChunking;
	private final DocBookProfilingStrategy profiling;
	private final TargetNamingStrategy targetNamingStrategy;

	public FormatPlan(
			final UserFormatConfiguration userFormatConfiguration,
			final StandardDocBookFormatDescriptor docBookFormatDescriptor) {
		this(
				userFormatConfiguration,
				docBookFormatDescriptor,
				new TargetNamingStrategy() {
					public String determineTargetFileName(File source) {
						return userFormatConfiguration.getFinalName() == null
								? FileUtils.basename( source.getAbsolutePath() ) + docBookFormatDescriptor.getStandardFileExtension()
								: userFormatConfiguration.getFinalName();
					}
				}
		);
	}

	public FormatPlan(
			UserFormatConfiguration userFormatConfiguration,
			StandardDocBookFormatDescriptor docBookFormatDescriptor,
			TargetNamingStrategy targetNamingStrategy) {
		this.name = userFormatConfiguration.getFormatName();

		this.profiling = DocBookProfilingStrategy.parse( userFormatConfiguration.getProfilingTypeName() );
		this.stylesheetResource = userFormatConfiguration.getStylesheetResource() != null
				? userFormatConfiguration.getStylesheetResource()
				: DocBookProfilingStrategy.SINGLE_PASS == profiling
						? docBookFormatDescriptor.getProfiledStylesheetResource()
						: docBookFormatDescriptor.getStylesheetResource();
		this.correspondingDocBookStylesheetResource = DocBookProfilingStrategy.SINGLE_PASS == profiling
				? docBookFormatDescriptor.getProfiledStylesheetResource()
				: docBookFormatDescriptor.getStylesheetResource();

		this.imagePathSettingRequired = userFormatConfiguration.getImagePathSettingRequired() == null
				? docBookFormatDescriptor.isImagePathSettingRequired()
				: userFormatConfiguration.getImagePathSettingRequired();
		this.imageCopyingRequired = userFormatConfiguration.getImageCopyingRequired() == null
				? docBookFormatDescriptor.isImageCopyingRequired()
				: userFormatConfiguration.getImageCopyingRequired();
		this.doingChunking = userFormatConfiguration.getDoingChunking() == null
				? docBookFormatDescriptor.isDoingChunking()
				: userFormatConfiguration.getDoingChunking();
		this.targetNamingStrategy = targetNamingStrategy;
	}

	public FormatPlan(
			String name,
			String stylesheetResource,
			String correspondingDocBookStylesheetResource,
			boolean imagePathSettingRequired,
			boolean imageCopyingRequired,
			boolean doingChunking,
			DocBookProfilingStrategy profiling,
			TargetNamingStrategy targetNamingStrategy) {
		this.name = name;
		this.stylesheetResource = stylesheetResource;
		this.correspondingDocBookStylesheetResource = correspondingDocBookStylesheetResource;
		this.imagePathSettingRequired = imagePathSettingRequired;
		this.imageCopyingRequired = imageCopyingRequired;
		this.doingChunking = doingChunking;
		this.profiling = profiling;
		this.targetNamingStrategy = targetNamingStrategy;
	}

	public String getName() {
		return name;
	}

	public String getStylesheetResource() {
		return stylesheetResource;
	}

	public String getCorrespondingDocBookStylesheetResource() {
		return correspondingDocBookStylesheetResource;
	}

	public boolean isImagePathSettingRequired() {
		return imagePathSettingRequired;
	}

	public boolean isImageCopyingRequired() {
		return imageCopyingRequired;
	}

	public boolean isDoingChunking() {
		return doingChunking;
	}

	public TargetNamingStrategy getTargetNamingStrategy() {
		return targetNamingStrategy;
	}

	public DocBookProfilingStrategy getProfiling() {
		return profiling;
	}
}
