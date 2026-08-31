import type {
  BranchResponse,
  ClosestBranchResponse,
  DoctorResponse,
  AppointmentResponse,
  ClinicVisitResponse,
} from '../types';

export const SAMPLE_BRANCHES: BranchResponse[] = [
  {
    id: 1,
    organizationId: 'unilag-med-centre',
    name: 'UNILAG Medical Centre — Main Campus (Akoka)',
    address: 'Commercial Road, UNILAG Main Campus, Akoka, Yaba, Lagos',
    latitude: 6.5158,
    longitude: 3.3898,
  },
  {
    id: 2,
    organizationId: 'unilag-med-centre',
    name: 'UNILAG Medical Centre Annex — Idi-Araba (CMUL)',
    address: 'College of Medicine, University of Lagos, Idi-Araba, Lagos',
    latitude: 6.5192,
    longitude: 3.3571,
  },
  {
    id: 3,
    organizationId: 'unilag-med-centre',
    name: 'UNILAG Student Health Service Clinic',
    address: 'Near Faculty of Science, Akoka Campus, Yaba, Lagos',
    latitude: 6.5175,
    longitude: 3.3912,
  },
];

export const SAMPLE_CLOSEST_BRANCHES: ClosestBranchResponse[] = [
  {
    branch: SAMPLE_BRANCHES[0],
    distanceInKilometers: 0.3,
  },
  {
    branch: SAMPLE_BRANCHES[2],
    distanceInKilometers: 0.6,
  },
  {
    branch: SAMPLE_BRANCHES[1],
    distanceInKilometers: 4.2,
  },
];

export const SAMPLE_DOCTORS: DoctorResponse[] = [
  {
    id: 101,
    branchId: 1,
    firstName: 'Dr. Babatunde',
    lastName: 'Adeleke',
    specialty: 'General Practice & Student Health',
    isAvailable: true,
  },
  {
    id: 102,
    branchId: 1,
    firstName: 'Dr. Folashade',
    lastName: 'Olayemi',
    specialty: 'Internal Medicine & Infectious Diseases',
    isAvailable: true,
  },
  {
    id: 103,
    branchId: 2,
    firstName: 'Dr. Emeka',
    lastName: 'Okafor',
    specialty: 'Dental Surgery & Oral Health',
    isAvailable: true,
  },
  {
    id: 104,
    branchId: 1,
    firstName: 'Dr. Chioma',
    lastName: 'Eze',
    specialty: 'Optometry & Ophthalmology',
    isAvailable: false,
  },
];

export const SAMPLE_APPOINTMENTS: AppointmentResponse[] = [
  {
    id: 1042,
    userId: 'usr-patient-1',
    branchId: 1,
    doctorId: 101,
    status: 'IN_QUEUE',
    scheduledStartTime: new Date(Date.now() + 15 * 60000).toISOString(),
    scheduledEndTime: new Date(Date.now() + 45 * 60000).toISOString(),
    actualStartTime: null,
    actualEndTime: null,
  },
  {
    id: 1039,
    userId: 'usr-patient-2',
    branchId: 1,
    doctorId: 102,
    status: 'IN_PROGRESS',
    scheduledStartTime: new Date(Date.now() - 10 * 60000).toISOString(),
    scheduledEndTime: new Date(Date.now() + 20 * 60000).toISOString(),
    actualStartTime: new Date(Date.now() - 8 * 60000).toISOString(),
    actualEndTime: null,
  },
  {
    id: 1025,
    userId: 'usr-patient-3',
    branchId: 2,
    doctorId: 103,
    status: 'SCHEDULED',
    scheduledStartTime: new Date(Date.now() + 120 * 60000).toISOString(),
    scheduledEndTime: new Date(Date.now() + 150 * 60000).toISOString(),
    actualStartTime: null,
    actualEndTime: null,
  },
];

export const SAMPLE_VISITS: ClinicVisitResponse[] = [
  {
    id: 881,
    appointmentId: 994,
    userId: 'usr-patient-1',
    doctorId: 101,
    visitNotes: 'Student medical clearance & general consultation. Vital signs stable, BP 115/75, Temperature 36.6°C.',
    prescriptionDetails: 'Paracetamol 500mg (2 tabs TDS), Vitamin C 100mg daily.',
    recordedAt: new Date(Date.now() - 86400000 * 4).toISOString(),
  },
  {
    id: 852,
    appointmentId: 950,
    userId: 'usr-patient-1',
    doctorId: 102,
    visitNotes: 'Routine Malaria screening & blood test. Mild fever reported during examination week.',
    prescriptionDetails: 'Artemether-Lumefantrine (20/120mg) 4 tabs BD x 3 days. Rest recommended.',
    recordedAt: new Date(Date.now() - 86400000 * 18).toISOString(),
  },
];
